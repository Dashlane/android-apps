package com.dashlane.hermes.generated.events.user

import com.dashlane.hermes.TrackingLog
import com.dashlane.hermes.generated.definitions.DeviceAppearance
import javax.annotation.processing.Generated

@Generated
data class UpdateDeviceSettings(
    val deviceAppearance: DeviceAppearance? = null,
) : TrackingLog {
    override val name
        get() = "update_device_settings"

    override val category
        get() = TrackingLog.Category.USER

    override val priority
        get() = false

    override fun collect(collector: TrackingLog.Collector) {
        collector["name"] = name
        collector["device_appearance"] = deviceAppearance
    }
}