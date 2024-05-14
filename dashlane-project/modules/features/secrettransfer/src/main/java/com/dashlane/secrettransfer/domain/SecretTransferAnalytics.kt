package com.dashlane.secrettransfer.domain

import com.dashlane.hermes.LogRepository
import com.dashlane.hermes.generated.definitions.ActionDuringTransfer
import com.dashlane.hermes.generated.definitions.AnyPage
import com.dashlane.hermes.generated.definitions.BrowseComponent
import com.dashlane.hermes.generated.definitions.DeviceSelected
import com.dashlane.hermes.generated.definitions.TransferMethod
import com.dashlane.hermes.generated.events.user.TransferNewDevice
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecretTransferAnalytics @Inject constructor(
    private val logRepository: LogRepository
) {

    private var selectedTransferMethod: TransferMethod? = null
    private var selectedDevice: DeviceSelected? = null

    fun pageView(anyPage: AnyPage) {
        logRepository.queuePageView(BrowseComponent.MAIN_APP, anyPage)
    }

    fun selectTransferMethod(transferMethod: TransferMethod, deviceSelected: DeviceSelected) {
        selectedTransferMethod = transferMethod
        selectedDevice = deviceSelected

        logRepository.queueEvent(
            TransferNewDevice(
                transferMethod = transferMethod,
                loggedInDeviceSelected = deviceSelected,
                biometricsEnabled = false,
                action = ActionDuringTransfer.SELECT_TRANSFER_METHOD
            )
        )
    }

    fun completeDeviceTransfer(biometricsEnabled: Boolean) {
        if (selectedTransferMethod == null || selectedDevice == null) return
        logRepository.queueEvent(
            TransferNewDevice(
                transferMethod = selectedTransferMethod!!,
                loggedInDeviceSelected = selectedDevice!!,
                biometricsEnabled = biometricsEnabled,
                action = ActionDuringTransfer.COMPLETE_DEVICE_TRANSFER
            )
        )
        selectedTransferMethod = null
        selectedDevice = null
    }
}
