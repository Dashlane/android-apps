package com.dashlane.accountrecoverykey

import com.dashlane.user.UserAccountInfo
import com.dashlane.authentication.SecurityFeature
import com.dashlane.authentication.SettingsFactory
import com.dashlane.authentication.UserStorage
import com.dashlane.cryptography.Cryptography
import com.dashlane.cryptography.CryptographyKey
import com.dashlane.cryptography.CryptographyMarker
import com.dashlane.cryptography.EncryptedBase64String
import com.dashlane.cryptography.createEncryptionEngine
import com.dashlane.cryptography.encryptByteArrayToBase64String
import com.dashlane.hermes.LogRepository
import com.dashlane.hermes.generated.definitions.CreateKeyErrorName
import com.dashlane.hermes.generated.definitions.DeleteKeyReason
import com.dashlane.hermes.generated.definitions.FlowStep
import com.dashlane.hermes.generated.events.user.CreateAccountRecoveryKey
import com.dashlane.hermes.generated.events.user.DeleteAccountRecoveryKey
import com.dashlane.crypto.keys.userKeyBytes
import com.dashlane.network.tools.authorization
import com.dashlane.password.generator.PasswordGenerator
import com.dashlane.preference.UserPreferencesManager
import com.dashlane.server.api.DashlaneTime
import com.dashlane.server.api.Response
import com.dashlane.server.api.endpoints.accountrecovery.AccountRecoveryConfirmActivationService
import com.dashlane.server.api.endpoints.accountrecovery.AccountRecoveryDeactivateService
import com.dashlane.server.api.endpoints.accountrecovery.AccountRecoveryGetStatusService
import com.dashlane.server.api.endpoints.accountrecovery.AccountRecoveryRequestActivationService
import com.dashlane.session.Session
import com.dashlane.session.SessionManager
import com.dashlane.session.SessionObserver
import com.dashlane.session.UserDataRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

private const val SESSION_ERROR_MESSAGE = "Invalid session"

interface AccountRecoveryKeyRepository {

    val arkStatusFlow: SharedFlow<AccountRecoveryState>

    suspend fun getAccountRecoveryStatusAsync(): AccountRecoveryState
    suspend fun requestActivation(): Result<String>
    suspend fun confirmRecoveryKey(keyToBeConfirmed: String): Boolean
    suspend fun confirmActivation(): Result<Response<Unit>>
    suspend fun disableRecoveryKey(reason: DeleteKeyReason): Unit
}

@OptIn(ExperimentalCoroutinesApi::class)
@Singleton
class AccountRecoveryKeyRepositoryImpl @Inject constructor(
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
    private val logRepository: LogRepository,
    private val userPreferencesManager: UserPreferencesManager
) : AccountRecoveryKeyRepository {

    private val arkStatusMutableFlow: MutableSharedFlow<AccountRecoveryState> = MutableSharedFlow(replay = 1)
    override val arkStatusFlow: SharedFlow<AccountRecoveryState> = arkStatusMutableFlow.asSharedFlow()

    private var accountRecoveryKey: String? = null
    private var recoveryId: String? = null

    init {
        sessionManager.attach(object : SessionObserver {
            override suspend fun sessionEnded(session: Session, byUser: Boolean, forceLogout: Boolean) {
                super.sessionEnded(session, byUser, forceLogout)
                arkStatusMutableFlow.resetReplayCache()
                accountRecoveryKey = null
                recoveryId = null
            }
        })
    }

    override suspend fun getAccountRecoveryStatusAsync(): AccountRecoveryState {
        val state = getAccountRecoveryKeyStatus()
        arkStatusMutableFlow.emit(state)
        return state
    }

    override suspend fun requestActivation(): Result<String> {
        return runCatching {
            val session = sessionManager.session ?: throw IllegalStateException(SESSION_ERROR_MESSAGE)
            val accountRecoveryKey = generateAccountRecoveryKey()
            val encryptedVaultKey = encryptVaultKeyWithAccountRecoveryKey(accountRecoveryKey)
            val request = AccountRecoveryRequestActivationService.Request(encryptedVaultKey.value)
            val response = accountRecoveryRequestActivationService.execute(session.authorization, request)
            this.recoveryId = response.data.recoveryId
            return@runCatching accountRecoveryKey
        }
            .onFailure {
            }
    }

    override suspend fun confirmRecoveryKey(keyToBeConfirmed: String): Boolean {
        if (this.accountRecoveryKey != keyToBeConfirmed) {
            logRepository.queueEvent(
                CreateAccountRecoveryKey(
                    flowStep = FlowStep.ERROR,
                    createKeyErrorName = CreateKeyErrorName.WRONG_CONFIRMATION_KEY
                )
            )
            return false
        }

        val session = sessionManager.session ?: throw IllegalStateException(SESSION_ERROR_MESSAGE)

        val settingsManager = userDataRepository.getSettingsManager(session)
        val settings = settingsManager.getSettings().copy {
            accountRecoveryKey = keyToBeConfirmed
            accountRecoveryKeyId = recoveryId
        }

        settingsManager.updateSettings(settings)

        return true
    }

    override suspend fun confirmActivation(): Result<Response<Unit>> {
        return runCatching {
            val session = sessionManager.session ?: throw IllegalStateException(SESSION_ERROR_MESSAGE)
            val request =
                AccountRecoveryConfirmActivationService.Request(recoveryId = recoveryId ?: throw IllegalStateException("Invalid recoveryId"))
            accountRecoveryConfirmActivationService.execute(session.authorization, request)
        }
            .onSuccess {
                logRepository.queueEvent(CreateAccountRecoveryKey(flowStep = FlowStep.COMPLETE))

                val accountType = UserAccountInfo.AccountType.fromString(userPreferencesManager.accountType)
                if (accountType == UserAccountInfo.AccountType.InvisibleMasterPassword) userPreferencesManager.mplessARKEnabled = true
            }
            .onFailure {
            }
    }

    override suspend fun disableRecoveryKey(reason: DeleteKeyReason) {
        val session = sessionManager.session ?: throw IllegalStateException("Invalid session")

        val requestReason = when (reason) {
            DeleteKeyReason.NEW_RECOVERY_KEY_GENERATED -> AccountRecoveryDeactivateService.Request.Reason.SETTINGS
            DeleteKeyReason.RECOVERY_KEY_USED -> AccountRecoveryDeactivateService.Request.Reason.KEY_USED
            DeleteKeyReason.SETTING_DISABLED -> AccountRecoveryDeactivateService.Request.Reason.SETTINGS
            DeleteKeyReason.VAULT_KEY_CHANGED -> AccountRecoveryDeactivateService.Request.Reason.VAULT_KEY_CHANGE
        }

        val request = AccountRecoveryDeactivateService.Request(requestReason)
        accountRecoveryDeactivateService.execute(session.authorization, request)

        val settingsManager = userDataRepository.getSettingsManager(session)
        val settings = settingsManager.getSettings().copy {
            accountRecoveryKey = null
            accountRecoveryKeyId = null
        }

        settingsManager.updateSettings(settings)
        logRepository.queueEvent(DeleteAccountRecoveryKey(reason))
        arkStatusMutableFlow.emit(AccountRecoveryState.Success(visible = true, enabled = false))
    }

    private suspend fun getAccountRecoveryKeyStatus(): AccountRecoveryState {
        try {
            val session = sessionManager.session ?: throw IllegalStateException(SESSION_ERROR_MESSAGE)
            val securityFeatures = userStorage.getUser(session.userId)?.securityFeatures ?: throw Exception("No user")

            
            if (SecurityFeature.SSO in securityFeatures) {
                return AccountRecoveryState.Success(visible = false, enabled = false)
            }

            val request = AccountRecoveryGetStatusService.Request(session.userId)
            val getStatusResponse = accountRecoveryGetStatusService.execute(request)
            return AccountRecoveryState.Success(enabled = getStatusResponse.data.enabled, visible = true)
        } catch (e: Exception) {
            return AccountRecoveryState.Error(enabled = false, visible = false)
        }
    }

    private fun generateAccountRecoveryKey(): String {
        return passwordGenerator
            .generate(length = 28, digits = true, letters = true, symbols = false, ambiguousChars = true)
            .uppercase()
            .also { accountRecoveryKey = it }
    }

    private suspend fun encryptVaultKeyWithAccountRecoveryKey(accountRecoveryKey: String): EncryptedBase64String {
        val appKey = sessionManager.session?.appKey ?: throw IllegalStateException(SESSION_ERROR_MESSAGE)
        val cryptographyKey = CryptographyKey.ofPassword(accountRecoveryKey)
        val settings = settingsFactory.generateSettings(dashlaneTime.getClock().instant(), CryptographyMarker.Flexible.Defaults.argon2d)
        val encryptionEngine = cryptography.createEncryptionEngine(settings.cryptographyMarker, cryptographyKey, settings.cryptographyFixedSalt)
        return encryptionEngine.encryptByteArrayToBase64String(appKey.userKeyBytes.toByteArray())
    }
}

sealed class AccountRecoveryState {
    abstract val visible: Boolean
    abstract val enabled: Boolean

    data class Success(override val visible: Boolean, override val enabled: Boolean) : AccountRecoveryState()
    data class Error(override val visible: Boolean, override val enabled: Boolean) : AccountRecoveryState()
}
