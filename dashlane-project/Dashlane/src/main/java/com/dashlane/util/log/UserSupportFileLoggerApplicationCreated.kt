package com.dashlane.util.log

import android.app.Application
import com.dashlane.device.DeviceInfoRepository
import com.dashlane.preference.GlobalPreferencesManager
import com.dashlane.util.PackageUtilities
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class UserSupportFileLoggerApplicationCreated @Inject constructor(
    private val globalPreferencesManager: GlobalPreferencesManager,
    private val deviceInfoRepository: DeviceInfoRepository
) {

    fun onApplicationCreated(application: Application) {
        val userLoggedOut = globalPreferencesManager.isUserLoggedOut
        val isMultipleAccountLoadedOnThisDevice = globalPreferencesManager.isMultipleAccountLoadedOnThisDevice
        val lastLoggedInUser = globalPreferencesManager.getLastLoggedInUser()
        val anonymousDeviceId = deviceInfoRepository.anonymousDeviceId
        val deviceId = deviceInfoRepository.deviceId

        val appPackageInfo = PackageUtilities.getAppPackageInfo(application)
        val appInstallDate = appPackageInfo?.firstInstallTime?.toZonedDateTime()
        val appLastUpdateDate = appPackageInfo?.lastUpdateTime?.toZonedDateTime()

            "Dump device informations. " +
                    "userLoggedOut: $userLoggedOut, " +
                    "isMultipleAccountLoadedOnThisDevice: $isMultipleAccountLoadedOnThisDevice, " +
                    "lastLoggedInUser: $lastLoggedInUser, " +
                    "anonymousDeviceId: $anonymousDeviceId, " +
                    "deviceId: $deviceId, " +
                    "appInstallDate: $appInstallDate, " +
                    "appLastUpdateDate: $appLastUpdateDate",
            logToUserSupportFile = true
        )
    }

    private fun Long.toZonedDateTime(): String =
        ZonedDateTime.ofInstant(Instant.ofEpochMilli(this), ZoneId.systemDefault())
            .format(DateTimeFormatter.ISO_ZONED_DATE_TIME)
}
