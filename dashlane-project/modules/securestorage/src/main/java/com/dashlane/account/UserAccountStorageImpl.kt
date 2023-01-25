package com.dashlane.account

import com.dashlane.device.DeviceInfoRepository
import com.dashlane.preference.ConstantsPrefs
import com.dashlane.preference.UserPreferencesManager
import com.dashlane.session.Session
import com.dashlane.session.Username
import com.dashlane.storage.securestorage.UserSecureStorageManager
import javax.inject.Inject



class UserAccountStorageImpl @Inject constructor(
    private val userPreferencesManager: UserPreferencesManager,
    private val dataLossTrackingListener: UserAccountStorage.DataLossTrackingListener? = null,
    private val userSecureStorageManager: UserSecureStorageManager,
    private val deviceInfoRepository: DeviceInfoRepository
) : UserAccountStorage {

    override fun saveUserAccountInfo(
        userAccountInfo: UserAccountInfo,
        session: Session,
        allowOverwriteAccessKey: Boolean
    ) {
        val username = userAccountInfo.username
        val preferences = userPreferencesManager.preferencesFor(username)

        preferences.putBoolean(ConstantsPrefs.OTP2SECURITY, userAccountInfo.otp2)
        val oldAccessKey = preferences.accessKey
        val newAccessKey = userAccountInfo.accessKey
        if (oldAccessKey.isNullOrEmpty()) {
            
            dataLossTrackingListener?.logUserSupportFile("Initialize access key")
            preferences.accessKey = newAccessKey
        } else {
            
            if (oldAccessKey != newAccessKey) {
                if (allowOverwriteAccessKey) {
                    
                    dataLossTrackingListener?.logUserSupportFile("Force overwrite access key")
                    preferences.accessKey = newAccessKey
                } else {
                    dataLossTrackingListener?.logUserSupportFile("Attempted to overwrite access key")
                    val illegalStateException = IllegalStateException("Attempted to overwrite access key")
                    throw illegalStateException
                }
            }
        }

        userAccountInfo.securitySettings?.let {
            
            preferences.putLong(ConstantsPrefs.SECURITY_SETTINGS, it.asFlags().toLong())
        }
        userSecureStorageManager.storeSecretKey(session, session.secretKey)
    }

    

    override fun get(username: Username): UserAccountInfo? {
        val ukiStored = userSecureStorageManager.isSecretKeyStored(username)
        dataLossTrackingListener?.logUserSupportFile("read local data for '$username', uki: '$ukiStored'")
        return if (ukiStored) {
            val preferences = userPreferencesManager.preferencesFor(username)
            val userIsOTP2 = preferences.getBoolean(ConstantsPrefs.OTP2SECURITY)
            
            val securitySettingsFlags = preferences.getLong(ConstantsPrefs.SECURITY_SETTINGS).toInt()
            val securitySettings = if (securitySettingsFlags == 0) null else UserSecuritySettings(securitySettingsFlags)
            val accessKey = preferences.accessKey ?: deviceInfoRepository.deviceId

            dataLossTrackingListener?.logUserSupportFile(
                "account info for '$username', " +
                        "userIsOTP2: $userIsOTP2, " +
                        "null accessKey in preferences? ${preferences.accessKey == null}"
            )
            UserAccountInfo(username.email, userIsOTP2, securitySettings, accessKey)
        } else {
            null
        }
    }

    override fun saveSecuritySettings(username: Username, securitySettings: UserSecuritySettings) {
        val preferences = userPreferencesManager.preferencesFor(username)
        preferences.putLong(ConstantsPrefs.SECURITY_SETTINGS, securitySettings.asFlags().toLong())
    }
}