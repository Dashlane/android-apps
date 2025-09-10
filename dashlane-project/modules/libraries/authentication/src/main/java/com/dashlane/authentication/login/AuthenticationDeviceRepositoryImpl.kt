package com.dashlane.authentication.login

import com.dashlane.authentication.UserStorage
import com.dashlane.authentication.toAuthenticationSsoInfo
import com.dashlane.data.login.MigrationToSsoMemberInfo
import com.dashlane.data.login.toMigrationToSsoMemberInfo
import com.dashlane.server.api.Response
import com.dashlane.server.api.endpoints.authentication.AuthSsoInfo
import com.dashlane.server.api.endpoints.authentication.AuthVerification
import com.dashlane.server.api.endpoints.authentication.GetAuthenticationMethodsForLoginService
import com.dashlane.server.api.endpoints.authentication.exceptions.DeviceDeactivatedException
import com.dashlane.server.api.endpoints.authentication.exceptions.DeviceNotFoundException
import com.dashlane.server.api.exceptions.DashlaneApiException
import com.dashlane.server.api.exceptions.DashlaneApiHttpException
import javax.inject.Inject

class AuthenticationDeviceRepositoryImpl @Inject constructor(
    private val userStorage: UserStorage,
    private val getAuthenticationMethodsForLoginService: GetAuthenticationMethodsForLoginService,
) : AuthenticationDeviceRepository {
    override var restoredSessionMigrationToSsoMemberInfo: MigrationToSsoMemberInfo? = null

    override suspend fun getAccessKeyStatus(
        login: String,
        accessKey: String,
    ): AuthenticationDeviceRepository.AccessKeyStatus {
        val request = GetAuthenticationMethodsForLoginService.Request(
            login = login,
            deviceAccessKey = accessKey,
            profiles = listOf(
                GetAuthenticationMethodsForLoginService.Request.Profile(
                    login = login,
                    deviceAccessKey = accessKey,
                ),
            ),
            methods = emptyList(),
        )
        val response: Response<GetAuthenticationMethodsForLoginService.Data> = try {
            getAuthenticationMethodsForLoginService.execute(request)
        } catch (e: DeviceDeactivatedException) {
            userStorage.clearUser(login, "Device deactivated $login")
            return AuthenticationDeviceRepository.AccessKeyStatus.Revoked
        } catch (e: DeviceNotFoundException) {
            return AuthenticationDeviceRepository.AccessKeyStatus.Invalid
        } catch (e: DashlaneApiHttpException) {
            return if (e.errorCode == "unknown_device") {
                AuthenticationDeviceRepository.AccessKeyStatus.Invalid
            } else {
                AuthenticationDeviceRepository.AccessKeyStatus.Valid
            }
        } catch (e: DashlaneApiException) {
            return AuthenticationDeviceRepository.AccessKeyStatus.Valid
        }
        val responseData = response.data
        responseData.profilesToDelete.orEmpty().forEach {
            userStorage.clearUser(it.login, "Profile deleted ${it.login}")
        }

        restoredSessionMigrationToSsoMemberInfo = responseData
            .verifications
            .firstOrNull { it.type == AuthVerification.Type.SSO }
            ?.ssoInfo
            ?.toAuthenticationSsoInfo(login)
            ?.toMigrationToSsoMemberInfo()

        return AuthenticationDeviceRepository.AccessKeyStatus.Valid
    }
}