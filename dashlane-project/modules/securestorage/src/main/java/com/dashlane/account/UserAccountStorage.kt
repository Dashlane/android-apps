package com.dashlane.account

import com.dashlane.session.Session
import com.dashlane.session.Username

interface UserAccountStorage {

    operator fun get(username: String): UserAccountInfo? =
        Username.ofEmailOrNull(username)?.let(::get)

    operator fun get(username: Username): UserAccountInfo?

    fun saveUserAccountInfo(
        userAccountInfo: UserAccountInfo,
        session: Session,
        allowOverwriteAccessKey: Boolean
    )

    fun saveSecuritySettings(username: String, securitySettings: UserSecuritySettings) =
        saveSecuritySettings(Username.ofEmail(username), securitySettings)

    fun saveSecuritySettings(username: Username, securitySettings: UserSecuritySettings)
}