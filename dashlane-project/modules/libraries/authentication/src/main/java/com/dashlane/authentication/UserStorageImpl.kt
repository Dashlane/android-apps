package com.dashlane.authentication

import com.dashlane.account.UserAccountStorageImpl
import com.dashlane.user.UserSecuritySettings
import com.dashlane.device.DeviceInfoRepository
import com.dashlane.preference.PreferencesManager
import com.dashlane.user.Username
import javax.inject.Inject

class UserStorageImpl @Inject constructor(
    private val dataReset: LoginDataReset,
    private val userAccountStorage: UserAccountStorageImpl,
    private val preferencesManager: PreferencesManager,
    private val deviceInfoRepository: DeviceInfoRepository
) : UserStorage {
    override suspend fun clearUser(login: String, reason: String) {
        dataReset.clearData(Username.ofEmail(login), DataLossTrackingLogger.Reason.PASSWORD_CHANGED)
    }

    override fun getUser(login: String): UserStorage.UserDevice? {
        val userAccountInfo = userAccountStorage[login]
        return if (userAccountInfo == null) {
            null
        } else {
            val accessKey = preferencesManager[login].accessKey
                ?: deviceInfoRepository.deviceId
                ?: return null
            UserStorage.UserDevice(
                login,
                userAccountInfo.securitySettings.toSecurityFeatures(),
                accessKey,
                userAccountInfo.otp2
            )
        }
    }
}

fun UserSecuritySettings?.toSecurityFeatures(): Set<SecurityFeature> {
    this ?: return emptySet()
    return sequence {
        if (isToken) yield(SecurityFeature.EMAIL_TOKEN)
        if (isTotp) yield(SecurityFeature.TOTP)
        if (isDuoEnabled) yield(SecurityFeature.DUO)
        if (isU2fEnabled) yield(SecurityFeature.U2F)
        if (isSso) yield(SecurityFeature.SSO)
    }.toSet()
}
