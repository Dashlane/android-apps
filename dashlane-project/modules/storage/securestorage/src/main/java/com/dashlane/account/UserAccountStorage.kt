package com.dashlane.account

import com.dashlane.crypto.keys.LocalKey
import com.dashlane.user.UserAccountInfo
import com.dashlane.user.UserSecuritySettings
import com.dashlane.user.Username

interface UserAccountStorage {

    operator fun get(username: String): UserAccountInfo? =
        Username.ofEmailOrNull(username)?.let(::get)

    operator fun get(username: Username): UserAccountInfo?

    fun saveUserAccountInfo(
        userAccountInfo: UserAccountInfo,
        localKey: LocalKey,
        secretKey: String,
        allowOverwriteAccessKey: Boolean
    )

    fun saveSecuritySettings(username: String, securitySettings: UserSecuritySettings) =
        saveSecuritySettings(Username.ofEmail(username), securitySettings)

    fun saveSecuritySettings(username: Username, securitySettings: UserSecuritySettings)

    
    fun saveAccountType(username: String, accountType: UserAccountInfo.AccountType)
}