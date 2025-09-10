package com.dashlane.device

import android.content.Context
import com.dashlane.crashreport.CrashReporter
import com.dashlane.hardwaresecurity.BiometricRecovery
import com.dashlane.inappautofill.InAppLoginManager
import com.dashlane.preference.PreferencesManager
import com.dashlane.server.api.endpoints.devices.UpdateDeviceInfoService
import com.dashlane.session.Session
import com.dashlane.session.SessionManager
import com.dashlane.session.authorization
import com.dashlane.user.Username
import com.dashlane.util.tryOrNull
import com.dashlane.utils.coroutines.inject.qualifiers.ApplicationCoroutineScope
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceUpdateManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val service: UpdateDeviceInfoService,
    private val gson: Gson,
    private val preferencesManager: PreferencesManager,
    private val sessionManager: SessionManager,
    private val crashReporter: CrashReporter,
    private val biometricRecovery: BiometricRecovery,
    private val inAppLoginManager: InAppLoginManager,
    @ApplicationCoroutineScope private val coroutineScope: CoroutineScope,
) {
    private val PREF_DEVICE_INFORMATION = "device_information"

    @JvmOverloads
    fun updateIfNeeded(
        deviceInfo: DeviceInformation = generateDeviceInfo(),
    ) {
        sessionManager.session?.let { session ->
            updateIfNeeded(session, deviceInfo)
        }
    }

    private fun updateIfNeeded(session: Session, deviceInfo: DeviceInformation) {
        val deviceInfoStr = gson.toJson(deviceInfo)
        val deviceInformationLocal =
            tryOrNull { gson.fromJson(readJsonFromCache(session.username), DeviceInformation::class.java) }

        
        if (deviceInfo != deviceInformationLocal) {
            coroutineScope.launch {
                try {
                    val request = UpdateDeviceInfoService.Request(deviceInfoStr)
                    service.execute(session.authorization, request)
                    storeJsonInCache(session.username, gson.toJson(deviceInfo))
                } catch (t: Throwable) {
                        message = "DeviceUpdateManager could not update Device Info",
                        throwable = t,
                    )
                }
            }
        }
    }

    private fun generateDeviceInfo() = DeviceInformationGenerator(
        context,
        crashReporter,
        biometricRecovery,
        inAppLoginManager,
    ).generate()

    private fun readJsonFromCache(username: Username) =
        preferencesManager[username].getString(PREF_DEVICE_INFORMATION)

    private fun storeJsonInCache(username: Username, data: String) =
        preferencesManager[username].putString(PREF_DEVICE_INFORMATION, data)
}