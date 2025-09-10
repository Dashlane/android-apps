package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
enum class PlanChangeStep(override val code: String) : TrackingLog.Enum {
    CHANGE_PLAN_CTA(
        code = "change_plan_cta",
    ),
    CONFIRM_AND_PAY_CTA(
        code = "confirm_and_pay_cta",
    ),
    SELECT_PLAN_TIER(
        code = "select_plan_tier",
    ),
}