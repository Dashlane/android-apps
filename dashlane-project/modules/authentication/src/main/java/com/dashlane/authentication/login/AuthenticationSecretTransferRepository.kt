package com.dashlane.authentication.login

import com.dashlane.authentication.AuthenticationInvalidPasswordException
import com.dashlane.authentication.AuthenticationOfflineException
import com.dashlane.authentication.DeviceRegistrationInfo
import com.dashlane.authentication.createAppDecryptionEngine
import com.dashlane.authentication.createVaultDecryptionEngine
import com.dashlane.authentication.decryptRemoteKey
import com.dashlane.authentication.decryptSettings
import com.dashlane.cryptography.Cryptography
import com.dashlane.cryptography.CryptographyException
import com.dashlane.cryptography.CryptographyKey
import com.dashlane.cryptography.EncryptedBase64String
import com.dashlane.cryptography.asEncryptedBase64
import com.dashlane.cryptography.decryptBase64ToUtf8String
import com.dashlane.cryptography.forXml
import com.dashlane.server.api.ConnectivityCheck
import com.dashlane.server.api.endpoints.Platform
import com.dashlane.server.api.endpoints.authentication.AuthRegistrationAuthTicketService
import com.dashlane.server.api.endpoints.authentication.AuthRegistrationDevice
import com.dashlane.server.api.endpoints.authentication.PerformExtraDeviceVerificationService
import com.dashlane.session.AppKey
import com.dashlane.session.VaultKey
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.serializer.XmlException
import javax.inject.Inject

class AuthenticationSecretTransferRepository @Inject constructor(
    private val connectivityCheck: ConnectivityCheck,
    private val performExtraDeviceVerificationService: PerformExtraDeviceVerificationService,
    private val deviceRegistrationInfo: DeviceRegistrationInfo,
    private val completeRegistrationAuthTicketService: AuthRegistrationAuthTicketService,
    private val cryptography: Cryptography
) {

    suspend fun register(login: String, token: String): AuthRegistrationAuthTicketService.Data {
        if (connectivityCheck.isOffline) {
            throw AuthenticationOfflineException()
        }

        val performExtraDeviceVerificationRequest = PerformExtraDeviceVerificationService.Request(login = login, token = token)
        val performExtraDeviceVerificationResponse = performExtraDeviceVerificationService.execute(performExtraDeviceVerificationRequest)

        val completeRegistrationAuthTicketRequest = AuthRegistrationAuthTicketService.Request(
            login = login,
            authTicket = AuthRegistrationAuthTicketService.Request.AuthTicket(performExtraDeviceVerificationResponse.data.authTicket),
            device = AuthRegistrationDevice(
                osCountry = deviceRegistrationInfo.osCountry,
                temporary = false,
                appVersion = deviceRegistrationInfo.appVersion,
                deviceName = deviceRegistrationInfo.deviceName,
                platform = Platform.SERVER_ANDROID,
                osLanguage = deviceRegistrationInfo.osLanguage
            ),
        )

        val completeRegistrationAuthTicketResponse = completeRegistrationAuthTicketService.execute(completeRegistrationAuthTicketRequest)

        return completeRegistrationAuthTicketResponse.data
    }

    fun decryptStartTransferResponse(symmetricKey: ByteArray, encryptedData: String): String {
        return CryptographyKey.ofBytes64(symmetricKey)
            .use { key -> cryptography.createDecryptionEngine(key) }
            .use { engine -> engine.decryptBase64ToUtf8String(encryptedData.asEncryptedBase64()) }
    }

    fun decryptSettings(vaultKey: VaultKey.Password, encryptedSettings: String): SyncObject.Settings {
        return cryptography.createVaultDecryptionEngine(vaultKey).use { decryptionEngine ->
            try {
                decryptionEngine.forXml().decryptSettings(encryptedSettings.asEncryptedBase64())
            } catch (e: CryptographyException) {
                throw AuthenticationInvalidPasswordException(cause = e)
            } catch (e: XmlException) {
                throw AuthenticationInvalidPasswordException(cause = e)
            }
        }
    }

    fun decryptSettings(remoteKey: VaultKey.RemoteKey, encryptedSettings: String): SyncObject.Settings {
        return cryptography.createVaultDecryptionEngine(remoteKey).forXml()
            .use { it.decryptSettings(encryptedSettings.asEncryptedBase64()) }
    }

    fun decryptRemoteKey(ssoKey: AppKey.SsoKey, encryptedRemoteKey: EncryptedBase64String): VaultKey.RemoteKey {
        return cryptography.createAppDecryptionEngine(ssoKey)
            .use { it.decryptRemoteKey(encryptedRemoteKey) }
    }
}
