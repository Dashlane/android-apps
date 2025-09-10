package com.dashlane.hermes.generated.events.user

import com.dashlane.hermes.TrackingLog
import com.dashlane.hermes.generated.definitions.CancelPlanStep
import com.dashlane.hermes.generated.definitions.Plan
import com.dashlane.hermes.generated.definitions.PlanBillingPeriod
import com.dashlane.hermes.generated.definitions.SurveyAnswer
import javax.annotation.processing.Generated

@Generated
data class CancelPlan(
    val cancelPlanStep: CancelPlanStep,
    val surveyAnswer: SurveyAnswer? = null,
    val planBillingPeriod: PlanBillingPeriod,
    val plan: Plan,
) : TrackingLog {
    override val name
        get() = "cancel_plan"

    override val category
        get() = TrackingLog.Category.USER

    override val priority
        get() = false

    override fun collect(collector: TrackingLog.Collector) {
        collector["name"] = name
        collector["cancel_plan_step"] = cancelPlanStep
        collector["survey_answer"] = surveyAnswer
        collector["plan_billing_period"] = planBillingPeriod
        collector["plan"] = plan
    }
}