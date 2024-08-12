package com.dashlane.authentication.login

import com.dashlane.authentication.AuthenticationDeviceCredentialsInvalidException
import com.dashlane.authentication.AuthenticationEmptyPasswordException
import com.dashlane.authentication.AuthenticationInvalidPasswordException
import com.dashlane.authentication.DeviceRegistrationInfo
import com.dashlane.authentication.RegisteredUserDevice
import com.dashlane.authentication.SecurityFeature
import com.dashlane.authentication.UserStorage
import com.dashlane.authentication.createAppDecryptionEngine
import com.dashlane.authentication.createVaultDecryptionEngine
import com.dashlane.authentication.decryptBackupToken
import com.dashlane.authentication.decryptRemoteKey
import com.dashlane.authentication.decryptSettings
import com.dashlane.authentication.decryptSharingPrivateKey
import com.dashlane.authentication.localkey.AuthenticationLocalKeyRepository
import com.dashlane.authentication.toAuthenticationException
import com.dashlane.cryptography.Cryptography
import com.dashlane.cryptography.CryptographyException
import com.dashlane.cryptography.CryptographyMarker
import com.dashlane.cryptography.ObfuscatedByteArray
import com.dashlane.cryptography.SharingKeys
import com.dashlane.cryptography.asEncryptedBase64
import com.dashlane.cryptography.encodeUtf8ToObfuscated
import com.dashlane.cryptography.forXml
import com.dashlane.cryptography.isEmpty
import com.dashlane.server.api.Response
import com.dashlane.server.api.endpoints.Platform
import com.dashlane.server.api.endpoints.authentication.AuthLoginService
import com.dashlane.server.api.endpoints.authentication.AuthRegistrationDevice
import com.dashlane.server.api.endpoints.authentication.AuthRegistrationExtraDeviceService
import com.dashlane.server.api.endpoints.authentication.RemoteKey
import com.dashlane.server.api.endpoints.authentication.exceptions.DeviceDeactivatedException
import com.dashlane.server.api.endpoints.authentication.exceptions.DeviceNotFoundException
import com.dashlane.server.api.exceptions.DashlaneApiException
import com.dashlane.server.api.exceptions.DashlaneApiHttpException
import com.dashlane.server.api.exceptions.DashlaneApiIoException
import com.dashlane.server.api.time.toInstant
import com.dashlane.crypto.keys.AppKey
import com.dashlane.crypto.keys.LocalKey
import com.dashlane.user.Username
import com.dashlane.crypto.keys.VaultKey
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.serializer.XmlException
import java.time.Instant
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout

class AuthenticationPasswordRepositoryImpl(
    private val userStorage: UserStorage,
    private val authenticationLocalKeyRepository: AuthenticationLocalKeyRepository,
    private val loginService: AuthLoginService,
    private val extraDeviceService: AuthRegistrationExtraDeviceService,
    private val deviceRegistrationInfo: DeviceRegistrationInfo,
    private val cryptography: Cryptography
) : AuthenticationPasswordRepository {

    override suspend fun validate(
        registeredUserDevice: RegisteredUserDevice,
        passwordUtf8Bytes: ObfuscatedByteArray
    ): AuthenticationPasswordRepository.Result =
        withContext(Dispatchers.Default) { validateImpl(registeredUserDevice, passwordUtf8Bytes) }

    private suspend fun validateImpl(
        registeredUserDevice: RegisteredUserDevice,
        passwordUtf8Bytes: ObfuscatedByteArray
    ): AuthenticationPasswordRepository.Result {
        if (passwordUtf8Bytes.isEmpty) {
            throw AuthenticationEmptyPasswordException()
        }

        val applicationPassword = AppKey.Password(
            passwordUtf8Bytes,
            registeredUserDevice.serverKey?.encodeUtf8ToObfuscated()
        )
        return when (registeredUserDevice) {
            is RegisteredUserDevice.Local -> validateLocalUser(registeredUserDevice, applicationPassword)
            is RegisteredUserDevice.Remote -> validateRemoteUser(registeredUserDevice, applicationPassword)
            is RegisteredUserDevice.ToRestore -> validateToRestoreUser(registeredUserDevice, applicationPassword)
        }
    }

    private suspend fun validateLocalUser(
        registeredUserDevice: RegisteredUserDevice.Local,
        password: AppKey.Password
    ): AuthenticationPasswordRepository.Result = coroutineScope {
        
        
        
        val login = registeredUserDevice.login
        val username = Username.ofEmail(login)

        val deferredLocalKeyResult =
            async {
                runCatching { authenticationLocalKeyRepository.validateForLocal(username, password) }
            }

        val deferredDeviceValidity = async { getDeviceValidity(registeredUserDevice) }
        val localKeyResult = deferredLocalKeyResult.await()
        val deviceValidity = deferredDeviceValidity.await()

        if (deviceValidity == DeviceValidity.REVOKED || deviceValidity == DeviceValidity.UNRECOGNIZED) {
            val isValidPassword = localKeyResult.isSuccess
            throw AuthenticationDeviceCredentialsInvalidException(
                isValidPassword,
                deviceValidity == DeviceValidity.UNRECOGNIZED
            )
        } else {
            localKeyResult
                .fold(
                    onSuccess = { result ->
                        registeredUserDevice.toSuccess(result.secretKey, password, result.localKey)
                    },
                    onFailure = {
                        throw AuthenticationInvalidPasswordException(cause = it)
                    }
                )
        }
    }

    private enum class DeviceValidity {
        VALID,

        REVOKED,

        UNRECOGNIZED
    }

    private suspend fun getDeviceValidity(
        device: RegisteredUserDevice.Local
    ): DeviceValidity {
        val login = device.login
        val request = AuthLoginService.Request(
            login = login,
            deviceAccessKey = device.accessKey,
            profiles = listOf(
                AuthLoginService.Request.Profile(
                    login = device.login,
                    deviceAccessKey = device.accessKey
                )
            ),
            methods = emptyList()
        )
        val response: Response<AuthLoginService.Data> = try {
            withTimeout(TimeUnit.SECONDS.toMillis(2)) {
                loginService.execute(request)
            }
        } catch (e: DeviceDeactivatedException) {
            
            userStorage.clearUser(login, "Device deactivated $login")
            return DeviceValidity.REVOKED
        } catch (e: DeviceNotFoundException) {
            return DeviceValidity.UNRECOGNIZED
        } catch (e: DashlaneApiHttpException) {
            return if (e.errorCode == "unknown_device") {
                
                DeviceValidity.UNRECOGNIZED
            } else {
                
                DeviceValidity.VALID
            }
        } catch (e: DashlaneApiException) {
            
            return DeviceValidity.VALID
        } catch (e: TimeoutCancellationException) {
            
            return DeviceValidity.VALID
        }

        
        
        val responseData = response.data
        responseData.profilesToDelete.orEmpty().forEach {
            userStorage.clearUser(it.login, "Profile deleted ${it.login}")
        }

        return DeviceValidity.VALID
    }

    private suspend fun validateRemoteUser(
        registeredUserDevice: RegisteredUserDevice.Remote,
        password: AppKey.Password
    ): AuthenticationPasswordRepository.Result {
        val encryptedRemoteKey = registeredUserDevice.encryptedRemoteKey
        val vaultKey = if (encryptedRemoteKey == null) {
            password.toVaultKey()
        } else {
            try {
                cryptography.createAppDecryptionEngine(password).decryptRemoteKey(encryptedRemoteKey)
            } catch (e: CryptographyException) {
                throw AuthenticationInvalidPasswordException(cause = e)
            }
        }

        return cryptography.createVaultDecryptionEngine(vaultKey).use { decryptionEngine ->
            val userSettings = try {
                decryptionEngine.forXml().decryptSettings(registeredUserDevice.encryptedSettings.asEncryptedBase64())
            } catch (e: CryptographyException) {
                throw AuthenticationInvalidPasswordException(cause = e)
            } catch (e: XmlException) {
                throw AuthenticationInvalidPasswordException(cause = e)
            }

            val sharingKeys = registeredUserDevice.sharingKeys?.let {
                val privateKey = try {
                    decryptionEngine.decryptSharingPrivateKey(it.encryptedPrivateKey.asEncryptedBase64())
                } catch (e: CryptographyException) {
                    return@let null
                }

                SharingKeys(it.publicKey, privateKey)
            }
            val username = Username.ofEmail(registeredUserDevice.login)
            val localKey = authenticationLocalKeyRepository.createForRemote(
                username,
                password,
                CryptographyMarker.Flexible.Defaults.argon2d
            )
            registeredUserDevice.toSuccess(
                password,
                userSettings,
                registeredUserDevice.settingsDate,
                sharingKeys,
                localKey,
                vaultKey as? VaultKey.RemoteKey
            )
        }
    }

    private suspend fun validateToRestoreUser(
        userDevice: RegisteredUserDevice.ToRestore,
        vaultKey: AppKey.Password
    ): AuthenticationPasswordRepository.Result {
        val backupToken = try {
            cryptography.createVaultDecryptionEngine(vaultKey.toVaultKey())
                .use { it.decryptBackupToken(userDevice.cipheredBackupToken.asEncryptedBase64()) }
        } catch (e: CryptographyException) {
            throw AuthenticationDeviceCredentialsInvalidException(isValidPassword = false, cause = e)
        }
        val registeredDevice: RegisteredUserDevice.Remote = registerExtraDevice(userDevice.login, backupToken, userDevice.securityFeatures)
        return validateRemoteUser(registeredDevice, vaultKey)
    }

    private suspend fun registerExtraDevice(
        login: String,
        backupToken: String,
        securityFeatures: Set<SecurityFeature>
    ): RegisteredUserDevice.Remote {
        val request = AuthRegistrationExtraDeviceService.Request(
            login = login,
            device = AuthRegistrationDevice(
                osCountry = deviceRegistrationInfo.osCountry,
                temporary = false,
                appVersion = deviceRegistrationInfo.appVersion,
                deviceName = deviceRegistrationInfo.deviceName,
                platform = Platform.SERVER_ANDROID,
                osLanguage = deviceRegistrationInfo.osLanguage
            ),
            verification = AuthRegistrationExtraDeviceService.Request.Verification(
                AuthRegistrationExtraDeviceService.Request.Verification.Token(backupToken)
            )
        )
        val response = try {
            extraDeviceService.execute(request)
        } catch (e: DashlaneApiIoException) {
            throw e.toAuthenticationException()
        } catch (e: DashlaneApiException) {
            throw AuthenticationDeviceCredentialsInvalidException(isValidPassword = false, cause = e)
        }
        val data = response.data
        val encryptedSettings = data.settings.content!!
        val settingsDate = data.settings.backupDate.toInstant()
        
        return data.toRegisteredUserDevice(
            login = login,
            securityFeatures = securityFeatures,
            encryptedSettings = encryptedSettings,
            settingsDate = settingsDate
        )
    }
}

private fun RegisteredUserDevice.Remote.toSuccess(
    password: AppKey.Password,
    settings: SyncObject.Settings,
    settingsDate: Instant,
    sharingKeys: SharingKeys?,
    localKey: LocalKey,
    remoteKey: VaultKey.RemoteKey?
) = AuthenticationPasswordRepository.Result.Remote(
    login = login,
    securityFeatures = securityFeatures,
    accessKey = accessKey,
    secretKey = secretKey,
    password = password,
    settings = settings,
    settingsDate = settingsDate,
    registeredUserDevice = this,
    sharingKeys = sharingKeys,
    localKey = localKey,
    deviceAnalyticsId = deviceAnalyticsId,
    userAnalyticsId = userAnalyticsId,
    remoteKey = remoteKey
)

private fun RegisteredUserDevice.Local.toSuccess(
    secretKey: String,
    password: AppKey.Password,
    localKey: LocalKey
) = AuthenticationPasswordRepository.Result.Local(
    login = login,
    securityFeatures = securityFeatures,
    accessKey = accessKey,
    secretKey = secretKey,
    password = password,
    isAccessKeyRefreshed = false,
    localKey = localKey
)

private fun AuthRegistrationExtraDeviceService.Data.toRegisteredUserDevice(
    login: String,
    securityFeatures: Set<SecurityFeature>,
    encryptedSettings: String,
    settingsDate: Instant
) = RegisteredUserDevice.Remote(
    login = login,
    securityFeatures = securityFeatures,
    serverKey = serverKey,
    accessKey = deviceAccessKey,
    secretKey = deviceSecretKey,
    encryptedSettings = encryptedSettings,
    settingsDate = settingsDate,
    sharingKeys = sharingKeys?.toRegisteredDeviceSharingKeys(),
    userId = publicUserId,
    hasDesktopDevice = hasDesktopDevices,
    registeredDeviceCount = numberOfDevices,
    deviceAnalyticsId = deviceAnalyticsId,
    userAnalyticsId = userAnalyticsId,
    encryptedRemoteKey = remoteKeys?.findByTypeOrNull(RemoteKey.Type.MASTER_PASSWORD)
)
