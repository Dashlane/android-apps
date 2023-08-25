package com.dashlane.login.accountrecoverykey

import com.dashlane.accountrecoverykey.AccountRecoveryStatus
import com.dashlane.authentication.RegisteredUserDevice
import com.dashlane.cryptography.Cryptography
import com.dashlane.cryptography.CryptographyKey
import com.dashlane.cryptography.asEncryptedBase64
import com.dashlane.cryptography.decryptBase64ToUtf8String
import com.dashlane.server.api.endpoints.accountrecovery.AccountRecoveryGetEncryptedVaultKeyService
import com.dashlane.server.api.endpoints.accountrecovery.AccountRecoveryGetStatusService
import com.dashlane.server.api.endpoints.authentication.Auth2faUnauthenticatedSettingsService
import com.dashlane.server.api.endpoints.authentication.AuthSecurityType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoginAccountRecoveryKeyRepository @Inject constructor(
    private val accountRecoveryGetStatusService: AccountRecoveryGetStatusService,
    private val accountRecoveryGetEncryptedVaultKeyService: AccountRecoveryGetEncryptedVaultKeyService,
    private val auth2faUnauthenticatedSettingsService: Auth2faUnauthenticatedSettingsService,
    private val cryptography: Cryptography
) {

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
    ): Result<String> {
        return runCatching {
            val requestAuthTicket = AccountRecoveryGetEncryptedVaultKeyService.Request.AuthTicket(authTicket)
            val request = AccountRecoveryGetEncryptedVaultKeyService.Request(login, requestAuthTicket)
            val response = accountRecoveryGetEncryptedVaultKeyService.execute(request)
            val encryptedVaultKey = response.data.encryptedVaultKey.asEncryptedBase64()
            val cryptographyKey = CryptographyKey.ofPassword(accountRecoveryKey)
            val encryptionEngine = cryptography.createDecryptionEngine(cryptographyKey)
            encryptionEngine.decryptBase64ToUtf8String(encryptedVaultKey)
        }
            .onFailure {
            }
    }
}