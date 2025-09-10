package com.dashlane.hermes.generated.events.user

import com.dashlane.hermes.TrackingLog
import com.dashlane.hermes.generated.definitions.OnboardingTask
import javax.annotation.processing.Generated

@Generated
data class CompleteOnboardingTask(
    val onboardingTask: OnboardingTask,
) : TrackingLog {
    override val name
        get() = "complete_onboarding_task"

    override val category
        get() = TrackingLog.Category.USER

    override val priority
        get() = false

    override fun collect(collector: TrackingLog.Collector) {
        collector["name"] = name
        collector["onboarding_task"] = onboardingTask
    }
}