package com.dashlane.guidedonboarding

import com.dashlane.useractivity.log.usage.UsageLogCode75
import com.dashlane.useractivity.log.usage.UsageLogRepository

internal class GuidedOnboardingUsageLogger(private val usageLogRepository: UsageLogRepository?) :
    OnboardingQuestionnaireContract.Logger {

    override fun log(action: String, subtype: String?, subaction: String?) {
        usageLogRepository
            ?.enqueue(
                UsageLogCode75(
                    type = TYPE,
                    subtype = subtype,
                    action = action,
                    subaction = subaction
                )
            )
    }

    companion object {
        private const val TYPE = "guided_onboarding"
    }
}