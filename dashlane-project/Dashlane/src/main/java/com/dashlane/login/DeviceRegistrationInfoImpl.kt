package com.dashlane.login

import android.content.Context
import com.dashlane.BuildConfig
import com.dashlane.authentication.DeviceRegistrationInfo
import com.dashlane.device.DeviceInfoRepository
import com.dashlane.preference.ConstantsPrefs
import com.dashlane.preference.GlobalPreferencesManager
import com.dashlane.util.Constants
import com.dashlane.debug.DaDaDa
import com.dashlane.util.DeviceUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class DeviceRegistrationInfoImpl @Inject constructor(
    @ApplicationContext
    private val context: Context,
    private val deviceInfoRepository: DeviceInfoRepository,
    private val globalPreferencesManager: GlobalPreferencesManager,
    private val daDaDa: DaDaDa
) : DeviceRegistrationInfo {

    override val appVersion: String
        get() = daDaDa.appVersionName ?: BuildConfig.VERSION_NAME

    override val installOrigin: String?
        get() = globalPreferencesManager.getString(ConstantsPrefs.REFERRED_BY)

    override val deviceName: String
        get() = deviceInfoRepository.deviceName

    override val country: String
        get() = deviceInfoRepository.deviceCountry

    override val osCountry: String
        get() = DeviceUtils.getDeviceCountry(context)

    override val language: String
        get() = Constants.getLang()

    override val osLanguage: String
        get() = Constants.getOSLang()
}
