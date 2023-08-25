package com.dashlane.util

import androidx.annotation.MainThread
import com.dashlane.network.tools.authorization
import com.dashlane.server.api.DashlaneApi
import com.dashlane.server.api.endpoints.devices.DeactivateDevicesService
import com.dashlane.server.api.endpoints.devices.ListDevicesService.Data.Device
import com.dashlane.server.api.exceptions.DashlaneApiException
import com.dashlane.session.Session
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class DeviceListManager(
    coroutineContext: CoroutineContext,
    private val session: Session,
    dashlaneApi: DashlaneApi,
    private val refreshCallback: (List<Device>) -> Unit
) : CoroutineScope by CoroutineScope(coroutineContext + Dispatchers.Main.immediate) {

    private val listDevicesService = dashlaneApi
        .endpoints
        .devices
        .listDevicesService

    private val deactivateDevicesService = dashlaneApi
        .endpoints
        .devices
        .deactivateDevicesService

    @Suppress("EXPERIMENTAL_API_USAGE")
    private val actor = actor<Unit> {
        consumeEach {
            val response = try {
                listDevicesService.execute(session.authorization)
            } catch (ignored: DashlaneApiException) {
                
                return@consumeEach
            }
            val devices = response.data.devices.sortedByDescending(Device::updateDate)
            refreshCallback(devices)
        }
    }

    fun refresh() {
        try {
            actor.trySend(Unit)
        } catch (ignored: Throwable) {
            
        }
    }

    @MainThread
    fun deleteAsync(d: Device, successCallback: () -> Unit) {
        launch {
            try {
                deactivateDevicesService.execute(
                    session.authorization,
                    DeactivateDevicesService.Request(deviceIds = listOf(d.id))
                )
            } catch (e: DashlaneApiException) {
                
                return@launch
            }
            successCallback()
        }
    }
}
