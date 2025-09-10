package com.dashlane.authentication.login

import com.dashlane.data.login.MigrationToSsoMemberInfo

interface AuthenticationDeviceRepository {
    var restoredSessionMigrationToSsoMemberInfo: MigrationToSsoMemberInfo?

    suspend fun getAccessKeyStatus(
        login: String,
        accessKey: String,
    ): AccessKeyStatus

    sealed class AccessKeyStatus {
        data object Valid : AccessKeyStatus()

        data object Revoked : AccessKeyStatus()

        data object Invalid : AccessKeyStatus()
    }
}