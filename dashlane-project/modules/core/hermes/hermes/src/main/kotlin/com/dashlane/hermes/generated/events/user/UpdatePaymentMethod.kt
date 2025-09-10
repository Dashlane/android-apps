package com.dashlane.hermes.generated.events.user

import com.dashlane.hermes.TrackingLog
import com.dashlane.hermes.generated.definitions.FlowStep
import com.dashlane.hermes.generated.definitions.Plan
import com.dashlane.hermes.generated.definitions.PlanBillingPeriod
import javax.annotation.processing.Generated

@Generated
data class UpdatePaymentMethod(
    val flowStep: FlowStep,
    val planBillingPeriod: PlanBillingPeriod? = null,
    val plan: Plan? = null,
) : TrackingLog {
    override val name
        get() = "update_payment_method"

    override val category
        get() = TrackingLog.Category.USER

    override val priority
        get() = false

    override fun collect(collector: TrackingLog.Collector) {
        collector["name"] = name
        collector["flow_step"] = flowStep
        collector["plan_billing_period"] = planBillingPeriod
        collector["plan"] = plan
    }
}