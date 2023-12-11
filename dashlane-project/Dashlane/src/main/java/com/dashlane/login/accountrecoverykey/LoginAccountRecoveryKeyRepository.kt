package com.dashlane.login.accountrecoverykey

import com.dashlane.account.UserAccountInfo
import com.dashlane.accountrecoverykey.AccountRecoveryStatus
import com.dashlane.authentication.RegisteredUserDevice
import com.dashlane.cryptography.Cryptography
import com.dashlane.cryptography.CryptographyKey
import com.dashlane.cryptography.ObfuscatedByteArray
import com.dashlane.cryptography.asEncryptedBase64
import com.dashlane.cryptography.decryptBase64ToByteArray
import com.dashlane.cryptography.toObfuscated
import com.dashlane.server.api.Authorization
import com.dashlane.server.api.endpoints.accountrecovery.AccountRecoveryDeactivateService
import com.dashlane.server.api.endpoints.accountrecovery.AccountRecoveryGetEncryptedVaultKeyService
import com.dashlane.server.api.endpoints.accountrecovery.AccountRecoveryGetStatusService
import com.dashlane.server.api.endpoints.authentication.Auth2faUnauthenticatedSettingsService
import com.dashlane.server.api.endpoints.authentication.AuthSecurityType
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class LoginAccountRecoveryKeyData(
    val authTicket: String? = null,
    val registeredUserDevice: RegisteredUserDevice? = null,
    val accountType: UserAccountInfo.AccountType? = null,
    val obfuscatedVaultKey: ObfuscatedByteArray? = null,
    val newMasterPassword: ObfuscatedByteArray? = null,
    val pin: ObfuscatedByteArray? = null,
    val biometricEnabled: Boolean = false
)

@Singleton
class LoginAccountRecoveryKeyRepository @Inject constructor(
    private val accountRecoveryGetStatusService: AccountRecoveryGetStatusService,
    private val accountRecoveryGetEncryptedVaultKeyService: AccountRecoveryGetEncryptedVaultKeyService,
    private val accountRecoveryDeactivateService: AccountRecoveryDeactivateService,
    private val auth2faUnauthenticatedSettingsService: Auth2faUnauthenticatedSettingsService,
    private val cryptography: Cryptography
) {

    private val stateFlow = MutableStateFlow(LoginAccountRecoveryKeyData())
    val state = stateFlow.asStateFlow()

    suspend fun updateAccountType(accountType: UserAccountInfo.AccountType) {
        stateFlow.emit(stateFlow.value.copy(accountType = accountType))
    }

    suspend fun updateRegisteredDevice(registeredUserDevice: RegisteredUserDevice?, authTicket: String?) {
        stateFlow.emit(stateFlow.value.copy(registeredUserDevice = registeredUserDevice, authTicket = authTicket))
    }

    suspend fun updateObfuscatedVaultKey(obfuscatedVaultKey: ObfuscatedByteArray?) {
        stateFlow.emit(stateFlow.value.copy(obfuscatedVaultKey = obfuscatedVaultKey))
    }

    suspend fun updateNewMasterPassword(newMasterPassword: ObfuscatedByteArray?) {
        stateFlow.emit(stateFlow.value.copy(newMasterPassword = newMasterPassword))
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

    suspend fun getAccountRecoveryStatus(registeredUserDevice: RegisteredUserDevice): Result<AccountRecoveryStatus> {
        return runCatching {
            val request = AccountRecoveryGetStatusService.Request(registeredUserDevice.login)
            val getStatusResponse = accountRecoveryGetStatusService.execute(request)

            return@runCatching AccountRecoveryStatus(
                enabled = getStatusResponse.data.enabled,
                visible = true
            )
        }
            .onFailure {
            }
    }

    suspend fun get2FAStatusForRecovery(registeredUserDevice: RegisteredUserDevice.Local): AuthSecurityType {
        val request = Auth2faUnauthenticatedSettingsService.Request(registeredUserDevice.login)
        val responseData: Auth2faUnauthenticatedSettingsService.Data = auth2faUnauthenticatedSettingsService.execute(request).data
        return responseData.type
    }

    suspend fun verifyAccountRecoveryKey(
        login: String,
        accountRecoveryKey: String,
        authTicket: String
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

    suspend fun disableRecoveryKeyAfterUse(authorization: Authorization.User) {
        runCatching {
            val request = AccountRecoveryDeactivateService.Request(AccountRecoveryDeactivateService.Request.Reason.KEY_USED)
            accountRecoveryDeactivateService.execute(authorization, request)
        }.onFailure {
        }
    }
}
