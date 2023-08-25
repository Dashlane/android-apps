package com.dashlane.account

data class UserAccountInfo(
    val username: String,
    val otp2: Boolean,
    val securitySettings: UserSecuritySettings? = null,
    val accessKey: String
) {
    val sso: Boolean get() = securitySettings?.isSso == true
}