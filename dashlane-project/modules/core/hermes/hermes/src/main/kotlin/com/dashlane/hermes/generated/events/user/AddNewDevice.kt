package com.dashlane.hermes.generated.events.user

import com.dashlane.hermes.TrackingLog
import com.dashlane.hermes.generated.definitions.ActionDuringTransfer
import javax.annotation.processing.Generated

@Generated
data class AddNewDevice(
    val action: ActionDuringTransfer,
) : TrackingLog {
    override val name
        get() = "add_new_device"

    override val category
        get() = TrackingLog.Category.USER

    override val priority
        get() = false

    override fun collect(collector: TrackingLog.Collector) {
        collector["name"] = name
        collector["action"] = action
    }
}