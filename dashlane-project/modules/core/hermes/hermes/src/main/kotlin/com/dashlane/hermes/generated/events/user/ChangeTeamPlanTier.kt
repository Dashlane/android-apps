package com.dashlane.hermes.generated.events.user

import com.dashlane.hermes.TrackingLog
import com.dashlane.hermes.generated.definitions.B2bPlanTier
import com.dashlane.hermes.generated.definitions.PlanChangeStep
import javax.annotation.processing.Generated

@Suppress("kotlin:S1133")
@Deprecated("")
@Generated
data class ChangeTeamPlanTier(
    val nextBillingPlanTier: B2bPlanTier,
    val planChangeStep: PlanChangeStep,
    val currentPlanPaidSeatsCount: Int,
    val currentBillingPlanTier: B2bPlanTier,
    val hasPromo: Boolean? = null,
    val seatAddedCount: Int,
) : TrackingLog {
    override val name
        get() = "change_team_plan_tier"

    override val category
        get() = TrackingLog.Category.USER

    override val priority
        get() = false

    override fun collect(collector: TrackingLog.Collector) {
        collector["name"] = name
        collector["next_billing_plan_tier"] = nextBillingPlanTier
        collector["plan_change_step"] = planChangeStep
        collector["current_plan_paid_seats_count"] = currentPlanPaidSeatsCount
        collector["current_billing_plan_tier"] = currentBillingPlanTier
        collector["has_promo"] = hasPromo
        collector["seat_added_count"] = seatAddedCount
    }
}