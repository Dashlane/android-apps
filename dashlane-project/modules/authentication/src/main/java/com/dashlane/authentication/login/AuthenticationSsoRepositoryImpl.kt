package com.dashlane.authentication.login

import com.dashlane.authentication.AuthenticationInvalidSsoException
import com.dashlane.authentication.AuthenticationOfflineException
import com.dashlane.authentication.AuthenticationUnknownException
import com.dashlane.authentication.UserStorage
import com.dashlane.authentication.createAppDecryptionEngine
import com.dashlane.authentication.createVaultDecryptionEngine
import com.dashlane.authentication.decryptRemoteKey
import com.dashlane.authentication.decryptSettings
import com.dashlane.authentication.decryptSharingPrivateKey
import com.dashlane.authentication.localkey.AuthenticationLocalKeyRepository
import com.dashlane.authentication.login.AuthenticationSsoRepository.ValidateResult
import com.dashlane.authentication.toAuthenticationException
import com.dashlane.cryptography.Cryptography
import com.dashlane.cryptography.CryptographyBase64Exception
import com.dashlane.cryptography.CryptographyException
import com.dashlane.cryptography.CryptographyMarker
import com.dashlane.cryptography.EncryptedBase64String
import com.dashlane.cryptography.SharingKeys
import com.dashlane.cryptography.asEncryptedBase64
import com.dashlane.cryptography.decodeBase64ToByteArray
import com.dashlane.cryptography.decodeBase64ToByteArrayOrNull
import com.dashlane.cryptography.forXml
import com.dashlane.server.api.ConnectivityCheck
import com.dashlane.server.api.endpoints.authentication.AuthLoginService
import com.dashlane.server.api.endpoints.authentication.AuthRegistrationAuthTicketService
import com.dashlane.server.api.endpoints.authentication.AuthVerification
import com.dashlane.server.api.endpoints.authentication.RemoteKey
import com.dashlane.server.api.exceptions.DashlaneApiException
import com.dashlane.server.api.time.toInstant
import com.dashlane.session.AppKey
import com.dashlane.session.LocalKey
import com.dashlane.session.Username
import com.dashlane.session.VaultKey
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.serializer.XmlException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthenticationSsoRepositoryImpl(
    private val connectivityCheck: ConnectivityCheck,
    private val userStorage: UserStorage,
    private val loginService: AuthLoginService,
    private val authTicketHelper: AuthenticationAuthTicketHelper,
    private val authenticationLocalKeyRepository: AuthenticationLocalKeyRepository,
    private val cryptography: Cryptography
) : AuthenticationSsoRepository {
    override suspend fun getSsoInfo(login: String, accessKey: String): SsoInfo = getSsoInfoImpl(login, accessKey)

    private suspend fun getSsoInfoImpl(login: String, accessKey: String): SsoInfo {
        if (connectivityCheck.isOffline) {
            throw AuthenticationOfflineException()
        }

        val response = try {
            loginService.execute(
                AuthLoginService.Request(
                    login = login,
                    deviceAccessKey = accessKey,
                    profiles = listOf(
                        AuthLoginService.Request.Profile(
                            login = login,
                            deviceAccessKey = accessKey
                        )
                    ),
                    methods = emptyList()
                )
            )
        } catch (e: DashlaneApiException) {
            throw e.toAuthenticationException()
        }

        return response.data
            .verifications
            .firstOrNull { it.type == AuthVerification.Type.SSO }
            ?.ssoInfo?.toAuthenticationSsoInfo()
            ?: throw AuthenticationUnknownException(message = "response.serviceProviderUrl == null")
    }

    override suspend fun validate(
        login: String,
        ssoToken: String,
        serviceProviderKey: String,
        accessKey: String?
    ) = withContext(Dispatchers.Default) {
        validateImpl(
            login = login,
            ssoToken = ssoToken,
            serviceProviderKey = serviceProviderKey,
            accessKey = accessKey
        )
    }

    private suspend fun validateImpl(
        login: String,
        ssoToken: String,
        serviceProviderKey: String,
        accessKey: String?
    ): ValidateResult {
        if (connectivityCheck.isOffline) {
            throw AuthenticationOfflineException()
        }

        val userDevice = userStorage.getUser(login)

        if (accessKey != null && accessKey != userDevice?.accessKey) throw AuthenticationInvalidSsoException()

        val decodedServiceProverKey = try {
            serviceProviderKey.decodeBase64ToByteArray()
        } catch (e: CryptographyBase64Exception) {
            throw AuthenticationInvalidSsoException(cause = e)
        }

        return if (userDevice == null) {
            validateRemote(
                login = login,
                ssoToken = ssoToken,
                serviceProviderKey = decodedServiceProverKey
            )
        } else {
            validateLocal(
                login = login,
                ssoToken = ssoToken,
                serviceProviderKey = decodedServiceProverKey,
                accessKey = userDevice.accessKey
            )
        }
    }

    private suspend fun validateRemote(
        login: String,
        ssoToken: String,
        serviceProviderKey: ByteArray
    ): ValidateResult {
        val (authTicket, responseData) = try {
            authTicketHelper.verifySso(
                login = login,
                ssoToken = ssoToken
            ).run {
                authTicket to registerDevice()
            }
        } catch (e: DashlaneApiException) {
            throw e.toAuthenticationException()
        }

        val encryptedSettings = responseData.settings.content
        val serverKey = responseData.ssoServerKey?.decodeBase64ToByteArrayOrNull()
        val encryptedRemoteKey = responseData.remoteKeys?.findByTypeOrNull(RemoteKey.Type.SSO)

        if (encryptedSettings == null || serverKey == null || encryptedRemoteKey == null) throw AuthenticationInvalidSsoException()

        val ssoKey = try {
            AppKey.SsoKey.create(serverKey = serverKey, serviceProviderKey = serviceProviderKey)
        } catch (e: IllegalArgumentException) {
            throw AuthenticationInvalidSsoException(cause = e)
        }

        val remoteKey = try {
            decryptRemoteKey(ssoKey, encryptedRemoteKey)
        } catch (e: CryptographyException) {
            throw AuthenticationInvalidSsoException(cause = e)
        }

        val settings = try {
            decryptSettings(remoteKey, encryptedSettings)
        } catch (e: CryptographyException) {
            throw AuthenticationInvalidSsoException(cause = e)
        } catch (e: XmlException) {
            throw AuthenticationInvalidSsoException(cause = e)
        }

        val sharingKeys = decryptSharingKeys(remoteKey, responseData)

        val username = Username.ofEmail(login)
        val localKey = authenticationLocalKeyRepository.createForRemote(
            username,
            ssoKey,
            CryptographyMarker.Flexible.Defaults.noDerivation64
        )
        return responseData.toResult(login, ssoKey, authTicket, localKey, remoteKey, settings, sharingKeys)
    }

    private fun decryptRemoteKey(
        ssoKey: AppKey.SsoKey,
        encryptedRemoteKey: EncryptedBase64String
    ) =
        cryptography.createAppDecryptionEngine(ssoKey)
            .use { it.decryptRemoteKey(encryptedRemoteKey) }

    private fun decryptSettings(remoteKey: VaultKey.RemoteKey, encryptedSettings: String) =
        cryptography.createVaultDecryptionEngine(remoteKey).forXml()
            .use { it.decryptSettings(encryptedSettings.asEncryptedBase64()) }

    private fun decryptSharingKeys(
        remoteKey: VaultKey.RemoteKey,
        responseData: AuthRegistrationAuthTicketService.Data
    ): SharingKeys? {
        val sharingKeys = responseData.sharingKeys ?: return null
        val encryptedPrivateKey = sharingKeys.privateKey.asEncryptedBase64()
        val privateKey = try {
            cryptography.createVaultDecryptionEngine(remoteKey)
                .use { it.decryptSharingPrivateKey(encryptedPrivateKey) }
        } catch (_: Exception) {
            return null
        }
        return SharingKeys(sharingKeys.publicKey, privateKey)
    }

    private suspend fun validateLocal(
        login: String,
        accessKey: String,
        ssoToken: String,
        serviceProviderKey: ByteArray
    ): ValidateResult {
        val (authTicket, responseData) = try {
            authTicketHelper.verifySso(
                login = login,
                ssoToken = ssoToken
            ).run {
                authTicket to login(accessKey)
            }
        } catch (e: DashlaneApiException) {
            throw e.toAuthenticationException()
        }

        val ssoServerKey =
            responseData.ssoServerKey ?: throw AuthenticationInvalidSsoException("SSO server key is null")
        val serverKey = try {
            ssoServerKey.decodeBase64ToByteArray()
        } catch (e: CryptographyBase64Exception) {
            throw AuthenticationInvalidSsoException(cause = e)
        }

        val ssoKey = try {
            AppKey.SsoKey.create(serverKey = serverKey, serviceProviderKey = serviceProviderKey)
        } catch (e: IllegalArgumentException) {
            throw AuthenticationInvalidSsoException(cause = e)
        }

        val username = Username.ofEmail(login)
        val localKeyResult = runCatching {
            authenticationLocalKeyRepository.validateForLocal(username, ssoKey)
        }.onFailure {
            throw AuthenticationInvalidSsoException(cause = it)
        }.getOrThrow()

        return ValidateResult.Local(
            login = login,
            secretKey = localKeyResult.secretKey,
            localKey = localKeyResult.localKey,
            ssoKey = ssoKey,
            authTicket = authTicket
        )
    }
}

private fun AuthRegistrationAuthTicketService.Data.toResult(
    login: String,
    ssoKey: AppKey.SsoKey,
    authTicket: String,
    localKey: LocalKey,
    remoteKey: VaultKey.RemoteKey,
    settings: SyncObject.Settings,
    sharingKeys: SharingKeys?
): ValidateResult = ValidateResult.Remote(
    login = login,
    accessKey = deviceAccessKey,
    secretKey = deviceSecretKey,
    localKey = localKey,
    ssoKey = ssoKey,
    authTicket = authTicket,
    remoteKey = remoteKey,
    settings = settings,
    settingsDate = this.settings.backupDate.toInstant(),
    sharingKeys = sharingKeys,
    deviceAnalyticsId = deviceAnalyticsId,
    userAnalyticsId = userAnalyticsId
)