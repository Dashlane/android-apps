package com.dashlane.authentication



interface UserStorage {
    fun getUser(login: String): UserDevice?
    suspend fun clearUser(login: String, reason: String)

    data class UserDevice(
        val login: String,
        val securityFeatures: Set<SecurityFeature>,
        val accessKey: String,
        val isServerKeyNeeded: Boolean
    ) {
        val sso get() = SecurityFeature.SSO in securityFeatures
    }
}

internal fun UserStorage.UserDevice.toRegisteredUserDevice(
    serverKey: String? = null
) =
    RegisteredUserDevice.Local(
        login = login,
        securityFeatures = securityFeatures,
        serverKey = serverKey,
        accessKey = accessKey
    )