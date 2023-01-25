package com.dashlane.device

import android.content.Context
import com.dashlane.accountrecovery.AccountRecovery
import com.dashlane.crashreport.CrashReporter
import com.dashlane.inapplogin.InAppLoginManager
import com.dashlane.preference.UserPreferencesManager
import com.dashlane.session.SessionManager
import com.dashlane.sharing.service.ObjectToJson
import com.dashlane.storage.DataStorageProvider
import com.dashlane.util.JsonSerialization
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceUpdateManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val service: DeviceService,
    private val jsonSerialization: JsonSerialization,
    private val preferencesManager: UserPreferencesManager,
    private val sessionManager: SessionManager,
    private val crashReporter: CrashReporter,
    private val accountRecovery: AccountRecovery,
    private val inAppLoginManager: InAppLoginManager,
    private val dataStorageProvider: DataStorageProvider
) : DeviceUpdateCallback.Listener {

    private val PREF_DEVICE_INFORMATION = "device_information"

    lateinit var deviceInformation: DeviceInformation

    fun updateIfNeeded() {
        val session = sessionManager.session
        val login = session?.userId
        val uki = session?.uki
        if (login != null && uki != null) {
            updateIfNeeded(
                login, uki,
                DeviceInformationGenerator(
                    context,
                    crashReporter,
                    accountRecovery,
                    inAppLoginManager,
                    dataStorageProvider
                ).generate()
            )
        }
    }

    override fun onSuccess() {
        storeJsonInCache(jsonSerialization.toJson(deviceInformation))
    }

    override fun onFail() {
        
    }

    internal fun updateIfNeeded(login: String, uki: String, deviceInfo: DeviceInformation) {
        deviceInformation = deviceInfo
        val deviceInformationLocal = readJsonFromCache()

        
        if (deviceInformationLocal.isNullOrEmpty() ||
            jsonSerialization.toJson(deviceInfo) != deviceInformationLocal
        ) {
            execute(login, uki, deviceInfo)
        }
    }

    private fun execute(login: String, uki: String, deviceInformation: DeviceInformation) {
        service.updateDeviceInformation(
            login,
            uki,
            ObjectToJson(deviceInformation, jsonSerialization)
        )
            .enqueue(DeviceUpdateCallback(this))
    }

    private fun readJsonFromCache() =
        preferencesManager.getString(PREF_DEVICE_INFORMATION)

    private fun storeJsonInCache(data: String) =
        preferencesManager.putString(PREF_DEVICE_INFORMATION, data)
}
