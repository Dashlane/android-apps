package com.dashlane.hermes.generated.events.user

import com.dashlane.hermes.TrackingLog
import com.dashlane.hermes.generated.definitions.ActionDuringTransfer
import com.dashlane.hermes.generated.definitions.DeviceSelected
import com.dashlane.hermes.generated.definitions.TransferMethod
import javax.annotation.processing.Generated

@Generated
data class TransferNewDevice(
    val transferMethod: TransferMethod,
    val biometricsEnabled: Boolean,
    val action: ActionDuringTransfer,
    val loggedInDeviceSelected: DeviceSelected,
) : TrackingLog {
    override val name
        get() = "transfer_new_device"

    override val category
        get() = TrackingLog.Category.USER

    override val priority
        get() = false

    override fun collect(collector: TrackingLog.Collector) {
        collector["name"] = name
        collector["transfer_method"] = transferMethod
        collector["biometrics_enabled"] = biometricsEnabled
        collector["action"] = action
        collector["logged_in_device_selected"] = loggedInDeviceSelected
    }
}