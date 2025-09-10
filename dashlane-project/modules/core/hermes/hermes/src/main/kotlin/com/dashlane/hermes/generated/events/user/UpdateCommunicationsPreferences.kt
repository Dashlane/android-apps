package com.dashlane.hermes.generated.events.user

import com.dashlane.hermes.TrackingLog
import com.dashlane.hermes.generated.definitions.FlowStep
import javax.annotation.processing.Generated

@Generated
data class UpdateCommunicationsPreferences(
    val isMarketingOptIn: Boolean,
    val flowStep: FlowStep,
) : TrackingLog {
    override val name
        get() = "update_communications_preferences"

    override val category
        get() = TrackingLog.Category.USER

    override val priority
        get() = false

    override fun collect(collector: TrackingLog.Collector) {
        collector["name"] = name
        collector["is_marketing_opt_in"] = isMarketingOptIn
        collector["flow_step"] = flowStep
    }
}