package com.dashlane.account

import com.dashlane.device.DeviceInfoRepository
import com.dashlane.crypto.keys.LocalKey
import com.dashlane.preference.ConstantsPrefs
import com.dashlane.preference.PreferencesManager
import com.dashlane.user.Username
import com.dashlane.storage.securestorage.UserSecureStorageManager
import com.dashlane.user.UserAccountInfo
import com.dashlane.user.UserSecuritySettings
import javax.inject.Inject

class UserAccountStorageImpl @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val userSecureStorageManager: UserSecureStorageManager,
    private val deviceInfoRepository: DeviceInfoRepository
) : UserAccountStorage {

    override fun saveUserAccountInfo(
        userAccountInfo: UserAccountInfo,
        localKey: LocalKey,
        secretKey: String,
        allowOverwriteAccessKey: Boolean
    ) {
        val username = userAccountInfo.username
        val preferences = preferencesManager[username]

        preferences.putBoolean(ConstantsPrefs.OTP2SECURITY, userAccountInfo.otp2)
        saveAccountType(username = username, accountType = userAccountInfo.accountType)
        val oldAccessKey = preferences.accessKey
        val newAccessKey = userAccountInfo.accessKey
        if (oldAccessKey.isNullOrEmpty()) {
            
            preferences.accessKey = newAccessKey
        } else {
            
            if (oldAccessKey != newAccessKey) {
                if (allowOverwriteAccessKey) {
                    
                    preferences.accessKey = newAccessKey
                } else {
                    val illegalStateException = IllegalStateException("Attempted to overwrite access key")
                    throw illegalStateException
                }
            }
        }

        userAccountInfo.securitySettings?.let {
            
            preferences.putLong(ConstantsPrefs.SECURITY_SETTINGS, it.asFlags().toLong())
        }
        userSecureStorageManager.storeSecretKey(localKey, Username.ofEmail(username), secretKey)
    }

    override fun get(username: Username): UserAccountInfo? {
        val ukiStored = userSecureStorageManager.isSecretKeyStored(username)
        return if (ukiStored) {
            val preferences = preferencesManager[username]
            val userIsOTP2 = preferences.getBoolean(ConstantsPrefs.OTP2SECURITY)
            
            val securitySettingsFlags = preferences.getLong(ConstantsPrefs.SECURITY_SETTINGS).toInt()
            val securitySettings = if (securitySettingsFlags == 0) null else UserSecuritySettings(securitySettingsFlags)
            val accessKey = preferences.accessKey ?: deviceInfoRepository.deviceId ?: return null
            val accountType = preferences.accountType?.let { UserAccountInfo.AccountType.fromString(it) }

                "account info for '$username'," +
                    " userIsOTP2: $userIsOTP2, " +
                    "null accessKey in preferences? ${preferences.accessKey == null}",
                logToUserSupportFile = true
            )
            UserAccountInfo(
                username = username.email,
                otp2 = userIsOTP2,
                securitySettings = securitySettings,
                accessKey = accessKey,
                accountType = accountType ?: UserAccountInfo.AccountType.MasterPassword
            )
        } else {
            null
        }
    }

    override fun saveSecuritySettings(username: Username, securitySettings: UserSecuritySettings) {
        val preferences = preferencesManager[username]
        preferences.putLong(ConstantsPrefs.SECURITY_SETTINGS, securitySettings.asFlags().toLong())
    }

    override fun saveAccountType(username: String, accountType: UserAccountInfo.AccountType) {
        preferencesManager[username].accountType = accountType.toString()
    }
}
