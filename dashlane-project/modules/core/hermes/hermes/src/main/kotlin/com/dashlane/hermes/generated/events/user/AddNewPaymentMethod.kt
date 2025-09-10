package com.dashlane.hermes.generated.events.user

import com.dashlane.hermes.TrackingLog
import com.dashlane.hermes.generated.definitions.FlowStep
import javax.annotation.processing.Generated

@Generated
data class AddNewPaymentMethod(
    val flowStep: FlowStep,
) : TrackingLog {
    override val name
        get() = "add_new_payment_method"

    override val category
        get() = TrackingLog.Category.USER

    override val priority
        get() = false

    override fun collect(collector: TrackingLog.Collector) {
        collector["name"] = name
        collector["flow_step"] = flowStep
    }
}