package com.dashlane.login.accountrecoverykey

import com.dashlane.cryptography.Cryptography
import com.dashlane.cryptography.CryptographyKey
import com.dashlane.cryptography.ObfuscatedByteArray
import com.dashlane.cryptography.asEncryptedBase64
import com.dashlane.cryptography.decryptBase64ToByteArray
import com.dashlane.cryptography.toObfuscated
import com.dashlane.data.login.RegisteredUserDevice
import com.dashlane.preference.PreferencesManager
import com.dashlane.server.api.Authorization
import com.dashlane.server.api.endpoints.accountrecovery.AccountRecoveryDeactivateService
import com.dashlane.server.api.endpoints.accountrecovery.AccountRecoveryGetEncryptedVaultKeyService
import com.dashlane.server.api.endpoints.authentication.AuthSecurityType
import com.dashlane.server.api.endpoints.authentication.Get2FAStatusUnauthenticatedService
import com.dashlane.user.UserAccountInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

data class LoginAccountRecoveryKeyData(
    val accountType: UserAccountInfo.AccountType? = null,
    val obfuscatedVaultKey: ObfuscatedByteArray? = null,
    val pin: ObfuscatedByteArray? = null,
    val biometricEnabled: Boolean = false,
)

@Singleton
class LoginAccountRecoveryKeyRepository @Inject constructor(
    private val accountRecoveryGetEncryptedVaultKeyService: AccountRecoveryGetEncryptedVaultKeyService,
    private val accountRecoveryDeactivateService: AccountRecoveryDeactivateService,
    private val get2FAStatusUnauthenticatedService: Get2FAStatusUnauthenticatedService,
    private val cryptography: Cryptography,
    private val preferencesManager: PreferencesManager,
) {
    private val stateFlow = MutableStateFlow(LoginAccountRecoveryKeyData())
    val state = stateFlow.asStateFlow()

    suspend fun updateAccountType(accountType: UserAccountInfo.AccountType) {
        stateFlow.emit(stateFlow.value.copy(accountType = accountType))
    }

    suspend fun updateObfuscatedVaultKey(obfuscatedVaultKey: ObfuscatedByteArray?) {
        stateFlow.emit(stateFlow.value.copy(obfuscatedVaultKey = obfuscatedVaultKey))
    }

    suspend fun updatePin(pin: ObfuscatedByteArray?) {
        stateFlow.emit(stateFlow.value.copy(pin = pin))
    }

    suspend fun updateBiometricEnabled(biometricEnabled: Boolean) {
        stateFlow.emit(stateFlow.value.copy(biometricEnabled = biometricEnabled))
    }

    suspend fun clearData() {
        stateFlow.emit(LoginAccountRecoveryKeyData())
    }

    suspend fun get2FAStatusForRecovery(registeredUserDevice: RegisteredUserDevice.Local): AuthSecurityType {
        val request = Get2FAStatusUnauthenticatedService.Request(registeredUserDevice.login)
        val responseData: Get2FAStatusUnauthenticatedService.Data = get2FAStatusUnauthenticatedService.execute(request).data
        return responseData.type
    }

    suspend fun verifyAccountRecoveryKey(
        login: String,
        accountRecoveryKey: String,
        authTicket: String,
    ): Result<ObfuscatedByteArray> {
        return runCatching {
            val requestAuthTicket = AccountRecoveryGetEncryptedVaultKeyService.Request.AuthTicket(authTicket)
            val request = AccountRecoveryGetEncryptedVaultKeyService.Request(login, requestAuthTicket)
            val response = accountRecoveryGetEncryptedVaultKeyService.execute(request)
            val encryptedVaultKey = response.data.encryptedVaultKey.asEncryptedBase64()
            val cryptographyKey = CryptographyKey.ofPassword(accountRecoveryKey)
            val encryptionEngine = cryptography.createDecryptionEngine(cryptographyKey)
            encryptionEngine.decryptBase64ToByteArray(encryptedVaultKey).toObfuscated()
        }
            .onFailure {
            }
    }

    suspend fun disableRecoveryKeyAfterUse(accountType: UserAccountInfo.AccountType, authorization: Authorization.User) {
        runCatching {
            val request = AccountRecoveryDeactivateService.Request(AccountRecoveryDeactivateService.Request.Reason.KEY_USED)
            accountRecoveryDeactivateService.execute(authorization, request)

            if (accountType == UserAccountInfo.AccountType.InvisibleMasterPassword) {
                preferencesManager[authorization.login].mplessARKEnabled = false
            }
        }.onFailure {
        }
    }
}