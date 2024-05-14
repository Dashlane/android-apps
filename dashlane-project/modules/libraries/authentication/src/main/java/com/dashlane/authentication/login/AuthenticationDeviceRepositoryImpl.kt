package com.dashlane.authentication.login

import com.dashlane.authentication.UserStorage
import com.dashlane.server.api.Response
import com.dashlane.server.api.endpoints.authentication.AuthLoginService
import com.dashlane.server.api.endpoints.authentication.AuthVerification
import com.dashlane.server.api.endpoints.authentication.exceptions.DeviceDeactivatedException
import com.dashlane.server.api.endpoints.authentication.exceptions.DeviceNotFoundException
import com.dashlane.server.api.exceptions.DashlaneApiException
import com.dashlane.server.api.exceptions.DashlaneApiHttpException

class AuthenticationDeviceRepositoryImpl(
    private val userStorage: UserStorage,
    private val loginService: AuthLoginService
) : AuthenticationDeviceRepository {

    override suspend fun getAccessKeyStatus(
        login: String,
        accessKey: String
    ): AuthenticationDeviceRepository.AccessKeyStatus {
        val request = AuthLoginService.Request(
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
        val response: Response<AuthLoginService.Data> = try {
            loginService.execute(request)
        } catch (e: DeviceDeactivatedException) {
            userStorage.clearUser(login, "Device deactivated $login")
            return AuthenticationDeviceRepository.AccessKeyStatus.Revoked
        } catch (e: DeviceNotFoundException) {
            return AuthenticationDeviceRepository.AccessKeyStatus.Invalid
        } catch (e: DashlaneApiHttpException) {
            return if (e.errorCode == "unknown_device") {
                AuthenticationDeviceRepository.AccessKeyStatus.Invalid
            } else {
                AuthenticationDeviceRepository.AccessKeyStatus.Valid(ssoInfo = null)
            }
        } catch (e: DashlaneApiException) {
            return AuthenticationDeviceRepository.AccessKeyStatus.Valid(ssoInfo = null)
        }
        val responseData = response.data
        responseData.profilesToDelete.orEmpty().forEach {
            userStorage.clearUser(it.login, "Profile deleted ${it.login}")
        }

        return AuthenticationDeviceRepository.AccessKeyStatus.Valid(
            ssoInfo = responseData
                .verifications
                .firstOrNull { it.type == AuthVerification.Type.SSO }
                ?.ssoInfo
                ?.toAuthenticationSsoInfo()
        )
    }
}