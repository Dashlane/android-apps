package com.dashlane.guidedonboarding.darkwebmonitoring

import com.dashlane.useractivity.log.usage.UsageLogCode75
import com.dashlane.useractivity.log.usage.UsageLogRepository

internal class OnboardingDarkWebUsageLogger(private val usageLogRepository: UsageLogRepository?) {

    fun logEmailVerified() = log(EMAIL_VERIFIED_ACTION, "alerts")

    fun logNoAlerts() = log(EMAIL_VERIFIED_ACTION, "no_alerts")

    fun logEmailUnverified() = log(EMAIL_UNVERIFIED_ACTION, "display")

    fun logTryAgain() = log(EMAIL_UNVERIFIED_ACTION, "retry")

    fun logSkip() = log(EMAIL_UNVERIFIED_ACTION, "skip")

    private fun log(action: String, subaction: String) {
        usageLogRepository?.enqueue(
            UsageLogCode75(type = TYPE, subtype = SUBTYPE, action = action, subaction = subaction)
        )
    }

    companion object {
        private const val EMAIL_UNVERIFIED_ACTION = "email_unverified"
        private const val EMAIL_VERIFIED_ACTION = "email_verified"
        private const val TYPE = "guided_onboarding_dwm"
        private const val SUBTYPE = "confirmation"
    }
}