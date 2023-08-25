package com.dashlane.authentication.sso.utils

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserSsoInfo internal constructor(
    val key: String,
    val login: String,
    val ssoToken: String,
    val exists: Boolean
) : Parcelable

internal fun Uri.toUserSsoInfo(): UserSsoInfo? {
    val key = getQueryParameter("key") ?: return null
    val login = getQueryParameter("login") ?: return null
    val ssoToken = getQueryParameter("ssoToken") ?: return null

    return UserSsoInfo(
        key = key,
        login = login,
        ssoToken = ssoToken,
        exists = getBooleanQueryParameter("exists", false)
    )
}