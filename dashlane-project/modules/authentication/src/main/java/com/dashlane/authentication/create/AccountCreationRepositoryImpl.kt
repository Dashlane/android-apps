package com.dashlane.authentication.create

import com.dashlane.authentication.AuthenticationInvalidLoginException
import com.dashlane.authentication.AuthenticationUnknownException
import com.dashlane.authentication.DeviceRegistrationInfo
import com.dashlane.authentication.RemoteKeyFactory
import com.dashlane.authentication.Settings
import com.dashlane.authentication.SettingsFactory
import com.dashlane.authentication.SsoServerKeyFactory
import com.dashlane.authentication.TermsOfService
import com.dashlane.authentication.UuidFactory
import com.dashlane.authentication.createPasswordEncryptionEngine
import com.dashlane.authentication.createRemoteKeyEncryptionEngine
import com.dashlane.authentication.createSsoEncryptionEngine
import com.dashlane.authentication.encryptRemoteKey
import com.dashlane.authentication.encryptSettings
import com.dashlane.authentication.encryptSharingPrivateKey
import com.dashlane.authentication.toAuthenticationException
import com.dashlane.authentication.toConsentsList
import com.dashlane.authentication.toSyncObject
import com.dashlane.cryptography.Cryptography
import com.dashlane.cryptography.CryptographyBase64Exception
import com.dashlane.cryptography.CryptographyMarker
import com.dashlane.cryptography.EncryptionEngine
import com.dashlane.cryptography.ObfuscatedByteArray
import com.dashlane.cryptography.SharingCryptography
import com.dashlane.cryptography.SharingKeys
import com.dashlane.cryptography.decodeBase64ToByteArray
import com.dashlane.cryptography.encodeBase64ToString
import com.dashlane.cryptography.forXml
import com.dashlane.server.api.DashlaneTime
import com.dashlane.server.api.endpoints.Platform
import com.dashlane.server.api.endpoints.account.CreateAccountResult
import com.dashlane.server.api.endpoints.account.CreateAccountService
import com.dashlane.server.api.endpoints.account.CreateAccountSsoService
import com.dashlane.server.api.exceptions.DashlaneApiException
import com.dashlane.server.api.time.toInstantEpochSecond
import com.dashlane.session.AppKey
import com.dashlane.session.VaultKey
import com.dashlane.xml.domain.SyncObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.dashlane.server.api.endpoints.account.Settings as CreateAccountSettings
import com.dashlane.server.api.endpoints.account.SharingKeys as CreateAccountSharingKeys

class AccountCreationRepositoryImpl(
    private val createAccountService: CreateAccountService,
    private val dashlaneTime: DashlaneTime,
    private val deviceRegistrationInfo: DeviceRegistrationInfo,
    private val settingsFactory: SettingsFactory,
    private val uuidFactory: UuidFactory,
    private val createAccountSsoService: CreateAccountSsoService,
    private val cryptography: Cryptography,
    private val sharingCryptography: SharingCryptography,
    private val remoteKeyFactory: RemoteKeyFactory,
    private val ssoServerKeyFactory: SsoServerKeyFactory
) : AccountCreationRepository {

    override suspend fun createAccount(
        login: String,
        passwordUtf8Bytes: ObfuscatedByteArray,
        termsOfService: TermsOfService,
        withRemoteKey: Boolean,
        withLegacyCrypto: Boolean
    ): AccountCreationRepository.Result =
        withContext(Dispatchers.Default) { createAccountImpl(login, passwordUtf8Bytes, termsOfService, withRemoteKey, withLegacyCrypto) }

    private suspend fun createAccountImpl(
        login: String,
        password: ObfuscatedByteArray,
        termsOfService: TermsOfService,
        withRemoteKey: Boolean,
        withLegacyCrypto: Boolean
    ): AccountCreationRepository.Result {
        val requestLogin = try {
            CreateAccountService.Request.Login(login)
        } catch (e: IllegalArgumentException) {
            throw AuthenticationInvalidLoginException(cause = e)
        }

        val settings = generateSettings(if (withLegacyCrypto) CryptographyMarker.Kwc3 else CryptographyMarker.Flexible.Defaults.argon2d)
        val sharingKeys = sharingCryptography.generateSharingKeys()
        val vaultKey = VaultKey.Password(password)
        val (request, remoteKey) = if (withRemoteKey) {
            val rk = remoteKeyFactory.generateRemoteKey()
            createRequestWithRemoteKey(
                login,
                requestLogin,
                settings,
                sharingKeys,
                vaultKey,
                termsOfService,
                rk
            ) to rk
        } else {
            createRequest(
                login,
                requestLogin,
                settings,
                sharingKeys,
                vaultKey,
                termsOfService
            ) to null
        }
        val response = try {
            createAccountService.execute(request)
        } catch (e: DashlaneApiException) {
            throw e.toAuthenticationException()
        }

        return response.data.toSuccess(
            login = login,
            settings = settings.toSyncObject(),
            sharingKeys = sharingKeys,
            remoteKey = remoteKey,
            appKey = AppKey.Password(password)
        )
    }

    private fun createRequest(
        login: String,
        loginRequest: CreateAccountService.Request.Login,
        settings: Settings,
        sharingKeys: SharingKeys,
        vaultKey: VaultKey.Password,
        termsOfService: TermsOfService
    ) = cryptography.createPasswordEncryptionEngine(vaultKey, settings).use { encryptionEngine ->
        createRequest(encryptionEngine, settings, login, sharingKeys, loginRequest, termsOfService, remoteKeys = null)
    }

    private fun createRequestWithRemoteKey(
        login: String,
        loginRequest: CreateAccountService.Request.Login,
        settings: Settings,
        sharingKeys: SharingKeys,
        vaultKey: VaultKey.Password,
        termsOfService: TermsOfService,
        remoteKey: VaultKey.RemoteKey
    ) = cryptography.createRemoteKeyEncryptionEngine(remoteKey).use { encryptionEngine ->
        createRequest(
            encryptionEngine,
            settings,
            login,
            sharingKeys,
            loginRequest,
            termsOfService,
            remoteKeys = listOf(
                CreateAccountService.Request.RemoteKey(
                    type = CreateAccountService.Request.RemoteKey.Type.MASTER_PASSWORD,
                    uuid = CreateAccountService.Request.RemoteKey.Uuid(uuidFactory.generateUuid()),
                    key = cryptography.createPasswordEncryptionEngine(vaultKey, settings)
                        .use { it.encryptRemoteKey(remoteKey) }.value
                )
            )
        )
    }

    private fun createRequest(
        encryptionEngine: EncryptionEngine,
        settings: Settings,
        login: String,
        sharingKeys: SharingKeys,
        loginRequest: CreateAccountService.Request.Login,
        termsOfService: TermsOfService,
        remoteKeys: List<CreateAccountService.Request.RemoteKey>?
    ) = CreateAccountService.Request(
        settings = settings.toCreateAccountSettings(encryptionEngine),
        country = deviceRegistrationInfo.country,
        appVersion = deviceRegistrationInfo.appVersion,
        contactEmail = CreateAccountService.Request.ContactEmail(login),
        origin = deviceRegistrationInfo.installOrigin,
        sharingKeys = CreateAccountSharingKeys(
            publicKey = sharingKeys.public.value,
            privateKey = encryptionEngine.encryptSharingPrivateKey(sharingKeys.private).value
        ),
        language = deviceRegistrationInfo.language,
        login = loginRequest,
        deviceName = deviceRegistrationInfo.deviceName,
        platform = Platform.SERVER_ANDROID,
        osCountry = deviceRegistrationInfo.osCountry,
        consents = termsOfService.toConsentsList(),
        sdkVersion = CreateAccountService.Request.SdkVersion(SDK_VERSION),
        osLanguage = deviceRegistrationInfo.osLanguage,
        remoteKeys = remoteKeys
    )

    override suspend fun createSsoAccount(
        login: String,
        ssoToken: String,
        serviceProviderKey: String,
        termsOfService: TermsOfService
    ) = withContext(Dispatchers.Default) {
        createSsoAccountImpl(
            login = login,
            ssoToken = ssoToken,
            serviceProviderKey = serviceProviderKey,
            termsOfService = termsOfService
        )
    }

    private suspend fun createSsoAccountImpl(
        login: String,
        ssoToken: String,
        serviceProviderKey: String,
        termsOfService: TermsOfService
    ): AccountCreationRepository.Result {
        try {
            CreateAccountSsoService.Request.Login(login)
        } catch (e: IllegalArgumentException) {
            throw AuthenticationInvalidLoginException(cause = e)
        }

        val decodedServiceProviderKey = try {
            serviceProviderKey.decodeBase64ToByteArray()
        } catch (e: CryptographyBase64Exception) {
            throw AuthenticationUnknownException(message = "Cannot decode Base 64", cause = e)
        }

        val ssoServerKey = ssoServerKeyFactory.generateSsoServerKey()
        val settings = generateSettings(CryptographyMarker.Flexible.Defaults.noDerivation64)
        val remoteKey = remoteKeyFactory.generateRemoteKey()
        val sharingKeys = sharingCryptography.generateSharingKeys()
        val ssoKey = try {
            AppKey.SsoKey.create(serverKey = ssoServerKey, serviceProviderKey = decodedServiceProviderKey)
        } catch (e: IllegalArgumentException) {
            throw AuthenticationUnknownException(cause = e)
        }

        val request = createSsoRequest(
            login = login,
            settings = settings,
            remoteKey = remoteKey,
            ssoServerKey = ssoServerKey,
            sharingKeys = sharingKeys,
            ssoKey = ssoKey,
            termsOfService = termsOfService,
            ssoToken = ssoToken
        )

        val response = try {
            createAccountSsoService.execute(request)
        } catch (e: DashlaneApiException) {
            throw e.toAuthenticationException()
        }

        return response.data.toSuccess(
            login = login,
            settings = settings.toSyncObject(),
            sharingKeys = sharingKeys,
            remoteKey = remoteKey,
            appKey = ssoKey
        )
    }

    private fun createSsoRequest(
        login: String,
        settings: Settings,
        remoteKey: VaultKey.RemoteKey,
        ssoServerKey: ByteArray,
        sharingKeys: SharingKeys,
        ssoKey: AppKey.SsoKey,
        termsOfService: TermsOfService,
        ssoToken: String
    ) = cryptography.createRemoteKeyEncryptionEngine(remoteKey).use { encryptionEngine ->
        CreateAccountSsoService.Request(
            settings = settings.toCreateAccountSettings(encryptionEngine),
            country = deviceRegistrationInfo.country,
            appVersion = deviceRegistrationInfo.appVersion,
            ssoServerKey = ssoServerKey.encodeBase64ToString(),
            contactEmail = CreateAccountSsoService.Request.ContactEmail(login),
            sharingKeys = CreateAccountSharingKeys(
                publicKey = sharingKeys.public.value,
                privateKey = encryptionEngine.encryptSharingPrivateKey(sharingKeys.private).value
            ),
            language = deviceRegistrationInfo.language,
            login = CreateAccountSsoService.Request.Login(login),
            deviceName = deviceRegistrationInfo.deviceName,
            platform = Platform.SERVER_ANDROID,
            osCountry = deviceRegistrationInfo.osCountry,
            remoteKeys = listOf(
                CreateAccountSsoService.Request.RemoteKey(
                    type = CreateAccountSsoService.Request.RemoteKey.Type.SSO,
                    uuid = CreateAccountSsoService.Request.RemoteKey.Uuid(uuidFactory.generateUuid()),
                    key = cryptography.createSsoEncryptionEngine(ssoKey).use { it.encryptRemoteKey(remoteKey) }.value
                )
            ),
            consents = termsOfService.toConsentsList(),
            sdkVersion = CreateAccountSsoService.Request.SdkVersion(SDK_VERSION),
            ssoToken = ssoToken,
            osLanguage = deviceRegistrationInfo.osLanguage
        )
    }

    private suspend fun generateSettings(marker: CryptographyMarker) = settingsFactory.generateSettings(
        time = dashlaneTime.getClock().instant(),
        cryptographyMarker = marker
    )

    private fun Settings.toCreateAccountSettings(encryptionEngine: EncryptionEngine) = CreateAccountSettings(
        time = time.toInstantEpochSecond(),
        content = encryptionEngine.forXml().encryptSettings(toSyncObject()).value
    )

    companion object {
        

        private const val SDK_VERSION = "1.0.0.0"
    }
}

private fun CreateAccountResult.toSuccess(
    login: String,
    settings: SyncObject.Settings,
    sharingKeys: SharingKeys,
    remoteKey: VaultKey.RemoteKey?,
    appKey: AppKey
) = AccountCreationRepository.Result(
    login = login,
    settings = settings,
    accessKey = deviceAccessKey,
    secretKey = deviceSecretKey,
    sharingKeys = sharingKeys,
    isAccountReset = accountReset,
    origin = origin,
    remoteKey = remoteKey,
    appKey = appKey,
    deviceAnalyticsId = deviceAnalyticsId,
    userAnalyticsId = userAnalyticsId
)