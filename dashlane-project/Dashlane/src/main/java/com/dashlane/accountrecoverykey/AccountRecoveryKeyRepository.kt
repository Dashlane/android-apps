package com.dashlane.accountrecoverykey

import com.dashlane.authentication.SecurityFeature
import com.dashlane.authentication.SettingsFactory
import com.dashlane.authentication.UserStorage
import com.dashlane.cryptography.Cryptography
import com.dashlane.cryptography.CryptographyKey
import com.dashlane.cryptography.CryptographyMarker
import com.dashlane.cryptography.EncryptedBase64String
import com.dashlane.cryptography.createEncryptionEngine
import com.dashlane.cryptography.encryptByteArrayToBase64String
import com.dashlane.network.tools.authorization
import com.dashlane.password.generator.PasswordGenerator
import com.dashlane.server.api.DashlaneTime
import com.dashlane.server.api.Response
import com.dashlane.server.api.endpoints.accountrecovery.AccountRecoveryConfirmActivationService
import com.dashlane.server.api.endpoints.accountrecovery.AccountRecoveryDeactivateService
import com.dashlane.server.api.endpoints.accountrecovery.AccountRecoveryGetStatusService
import com.dashlane.server.api.endpoints.accountrecovery.AccountRecoveryRequestActivationService
import com.dashlane.session.SessionManager
import com.dashlane.session.repository.UserDataRepository
import com.dashlane.util.inject.qualifiers.ApplicationCoroutineScope
import com.dashlane.util.userfeatures.UserFeaturesChecker
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

private const val SESSION_ERROR_MESSAGE = "Invalid session"

@Singleton
class AccountRecoveryKeyRepository @Inject constructor(
    private val userFeaturesChecker: UserFeaturesChecker,
    private val sessionManager: SessionManager,
    private val userStorage: UserStorage,
    private val accountRecoveryGetStatusService: AccountRecoveryGetStatusService,
    private val accountRecoveryRequestActivationService: AccountRecoveryRequestActivationService,
    private val accountRecoveryConfirmActivationService: AccountRecoveryConfirmActivationService,
    private val accountRecoveryDeactivateService: AccountRecoveryDeactivateService,
    private val cryptography: Cryptography,
    private val settingsFactory: SettingsFactory,
    private val dashlaneTime: DashlaneTime,
    private val passwordGenerator: PasswordGenerator,
    private val userDataRepository: UserDataRepository,
    @ApplicationCoroutineScope private val applicationScope: CoroutineScope
) {

    private var accountRecoveryStatus: Deferred<Result<AccountRecoveryStatus>>? = null

    private var accountRecoveryKey: String? = null
    private var recoveryId: String? = null

    
    fun getAccountRecoveryStatusBlocking(): AccountRecoveryStatus? = runBlocking {
        accountRecoveryStatus = accountRecoveryStatus ?: getAccountRecoveryStatusAsync()
        accountRecoveryStatus?.await()?.getOrNull()
    }

    suspend fun getAccountRecoveryStatusAsync(): Deferred<Result<AccountRecoveryStatus>> {
        return applicationScope.async(start = CoroutineStart.LAZY) {
            runCatching {
                val featureFlipEnabled = userFeaturesChecker.has(UserFeaturesChecker.FeatureFlip.ACCOUNT_RECOVERY_KEY)
                if (!featureFlipEnabled) return@runCatching AccountRecoveryStatus(visible = false)

                val session = sessionManager.session ?: throw IllegalStateException(SESSION_ERROR_MESSAGE)
                val securityFeatures = userStorage.getUser(session.userId)?.securityFeatures ?: throw Exception("No user")

                
                if (SecurityFeature.SSO in securityFeatures) return@runCatching AccountRecoveryStatus(visible = false)

                val request = AccountRecoveryGetStatusService.Request(session.userId)
                val getStatusResponse = accountRecoveryGetStatusService.execute(request)

                return@runCatching AccountRecoveryStatus(
                    enabled = getStatusResponse.data.enabled,
                    visible = true
                )
            }
                .onSuccess {
                    accountRecoveryStatus = CompletableDeferred(Result.success(it))
                }
                .onFailure {
                }
        }
    }

    suspend fun requestActivation(): Result<String> {
        return runCatching {
            val session = sessionManager.session ?: throw IllegalStateException(SESSION_ERROR_MESSAGE)
            val accountRecoveryKey = generateAccountRecoveryKey()
            val encryptedVaultKey = encryptVaultKeyWithAccountRecoveryKey(accountRecoveryKey)
            val request = AccountRecoveryRequestActivationService.Request(encryptedVaultKey.value)
            val response = accountRecoveryRequestActivationService.execute(session.authorization, request)
            this.recoveryId = response.data.recoveryId
            return@runCatching accountRecoveryKey
        }
    }

    suspend fun confirmRecoveryKey(keyToBeConfirmed: String): Boolean {
        if (this.accountRecoveryKey != keyToBeConfirmed) return false

        val session = sessionManager.session ?: throw IllegalStateException(SESSION_ERROR_MESSAGE)

        val settingsManager = userDataRepository.getSettingsManager(session)
        val settings = settingsManager.getSettings().copy {
            accountRecoveryKey = keyToBeConfirmed
            accountRecoveryKeyId = recoveryId
        }

        settingsManager.updateSettings(settings)

        return true
    }

    suspend fun confirmActivation(): Result<Response<Unit>> {
        return runCatching {
            val session = sessionManager.session ?: throw IllegalStateException(SESSION_ERROR_MESSAGE)
            val request =
                AccountRecoveryConfirmActivationService.Request(recoveryId = recoveryId ?: throw IllegalStateException("Invalid recoveryId"))
            accountRecoveryConfirmActivationService.execute(session.authorization, request)
        }
    }

    suspend fun disableRecoveryKey() {
        val session = sessionManager.session ?: throw IllegalStateException("Invalid session")

        val request = AccountRecoveryDeactivateService.Request(AccountRecoveryDeactivateService.Request.Reason.SETTINGS)
        accountRecoveryDeactivateService.execute(session.authorization, request)

        val settingsManager = userDataRepository.getSettingsManager(session)
        val settings = settingsManager.getSettings().copy {
            accountRecoveryKey = null
            accountRecoveryKeyId = null
        }

        settingsManager.updateSettings(settings)
    }

    private fun generateAccountRecoveryKey(): String {
        return passwordGenerator
            .generate(length = 28, digits = true, letters = true, symbols = false, ambiguousChars = true)
            .uppercase()
            .also { accountRecoveryKey = it }
    }

    private suspend fun encryptVaultKeyWithAccountRecoveryKey(accountRecoveryKey: String): EncryptedBase64String {
        val vaultKey = sessionManager.session?.vaultKey ?: throw IllegalStateException(SESSION_ERROR_MESSAGE)
        val cryptographyKey = CryptographyKey.ofPassword(accountRecoveryKey)
        val settings = settingsFactory.generateSettings(dashlaneTime.getClock().instant(), CryptographyMarker.Flexible.Defaults.argon2d)
        val encryptionEngine = cryptography.createEncryptionEngine(settings.cryptographyMarker, cryptographyKey, settings.cryptographyFixedSalt)
        return encryptionEngine.encryptByteArrayToBase64String(vaultKey.cryptographyKeyBytes.toByteArray())
    }
}

data class AccountRecoveryStatus(
    val visible: Boolean,
    val enabled: Boolean = false
)