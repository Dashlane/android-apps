package com.dashlane.hermes.generated.events.user

import com.dashlane.hermes.TrackingLog
import com.dashlane.hermes.generated.definitions.Plan
import com.dashlane.hermes.generated.definitions.PlanBillingPeriod
import javax.annotation.processing.Generated

@Generated
data class RestartPlan(
    val planBillingPeriod: PlanBillingPeriod,
    val plan: Plan,
) : TrackingLog {
    override val name
        get() = "restart_plan"

    override val category
        get() = TrackingLog.Category.USER

    override val priority
        get() = false

    override fun collect(collector: TrackingLog.Collector) {
        collector["name"] = name
        collector["plan_billing_period"] = planBillingPeriod
        collector["plan"] = plan
    }
}