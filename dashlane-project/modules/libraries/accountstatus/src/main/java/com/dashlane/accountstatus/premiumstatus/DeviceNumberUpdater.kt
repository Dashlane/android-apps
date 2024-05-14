package com.dashlane.accountstatus.premiumstatus

import com.dashlane.network.tools.authorization
import com.dashlane.preference.ConstantsPrefs
import com.dashlane.preference.UserPreferencesManager
import com.dashlane.server.api.endpoints.devices.ListDevicesService
import com.dashlane.server.api.exceptions.DashlaneApiException
import com.dashlane.session.SessionManager
import javax.inject.Inject

class DeviceNumberUpdater @Inject constructor(
    private val preferencesManager: UserPreferencesManager,
    private val listDevicesService: ListDevicesService,
    private val sessionManager: SessionManager,
) {
    suspend fun updateNumberOfDevices() {
        if (!preferencesManager.contains(ConstantsPrefs.USER_NUMBER_DEVICES)) {
            
            preferencesManager.putInt(ConstantsPrefs.USER_NUMBER_DEVICES, 1)
        }

        try {
            val session = sessionManager.session ?: throw IllegalArgumentException("session is null")
            val numberOfDevices = listDevicesService.execute(session.authorization)
                .data.devices.size

            preferencesManager.putInt(ConstantsPrefs.USER_NUMBER_DEVICES, numberOfDevices)
        } catch (e: DashlaneApiException) {
        } catch (e: IllegalArgumentException) {
        }
    }

    companion object {
        private const val UPDATE_DEVICE_NUMBER_ERROR = "Failed to update number of devices"
    }
}