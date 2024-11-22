package com.dashlane.accountstatus.premiumstatus

import com.dashlane.session.authorization
import com.dashlane.preference.ConstantsPrefs
import com.dashlane.preference.PreferencesManager
import com.dashlane.server.api.endpoints.devices.ListDevicesService
import com.dashlane.server.api.exceptions.DashlaneApiException
import com.dashlane.session.SessionManager
import javax.inject.Inject

class DeviceNumberUpdater @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val listDevicesService: ListDevicesService,
    private val sessionManager: SessionManager,
) {
    suspend fun updateNumberOfDevices() {
        val preferences = preferencesManager[sessionManager.session?.username]
        if (!preferences.contains(ConstantsPrefs.USER_NUMBER_DEVICES)) {
            
            preferences.putInt(ConstantsPrefs.USER_NUMBER_DEVICES, 1)
        }

        try {
            val session = sessionManager.session ?: throw IllegalArgumentException("session is null")
            val numberOfDevices = listDevicesService.execute(session.authorization)
                .data.devices.size

            preferences.putInt(ConstantsPrefs.USER_NUMBER_DEVICES, numberOfDevices)
        } catch (e: DashlaneApiException) {
        } catch (e: IllegalArgumentException) {
        }
    }

    companion object {
        private const val UPDATE_DEVICE_NUMBER_ERROR = "Failed to update number of devices"
    }
}