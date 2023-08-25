package com.dashlane.authentication.login

interface AuthenticationDeviceRepository {
    suspend fun getAccessKeyStatus(
        login: String,
        accessKey: String
    ): AccessKeyStatus

    sealed class AccessKeyStatus {
        data class Valid(val ssoInfo: SsoInfo?) : AccessKeyStatus()
        object Revoked : AccessKeyStatus()
        object Invalid : AccessKeyStatus()
    }
}
