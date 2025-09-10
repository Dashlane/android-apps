package com.dashlane.hermes.generated.events.user

import com.dashlane.hermes.TrackingLog
import com.dashlane.hermes.generated.definitions.OnboardingTask
import javax.annotation.processing.Generated

@Suppress("kotlin:S1133")
@Deprecated("")
@Generated
data class CompleteTacOnboardingTask(
    val onboardingTask: OnboardingTask,
) : TrackingLog {
    override val name
        get() = "complete_tac_onboarding_task"

    override val category
        get() = TrackingLog.Category.USER

    override val priority
        get() = false

    override fun collect(collector: TrackingLog.Collector) {
        collector["name"] = name
        collector["onboarding_task"] = onboardingTask
    }
}