package com.dashlane.hermes.generated.events.user

import com.dashlane.hermes.TrackingLog
import com.dashlane.hermes.generated.definitions.FlowStep
import com.dashlane.hermes.generated.definitions.ItemId
import javax.annotation.processing.Generated

@Generated
data class ChangePasswordGuided(
    val flowStep: FlowStep,
    val itemId: ItemId,
) : TrackingLog {
    override val name
        get() = "change_password_guided"

    override val category
        get() = TrackingLog.Category.USER

    override val priority
        get() = false

    override fun collect(collector: TrackingLog.Collector) {
        collector["name"] = name
        collector["flow_step"] = flowStep
        collector["item_id"] = itemId
    }
}