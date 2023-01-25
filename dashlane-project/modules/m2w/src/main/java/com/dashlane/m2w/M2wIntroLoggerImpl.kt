package com.dashlane.m2w

import com.dashlane.useractivity.log.usage.UsageLogRepository
import com.dashlane.useractivity.log.usage.UsageLogCode94

internal class M2wIntroLoggerImpl(
    private val usageLogRepository: UsageLogRepository?,
    private val origin: String
) : M2wIntroLogger {
    override fun logLand() = log(UsageLogCode94.Action.SEE)
    override fun logBack() = log(UsageLogCode94.Action.RETURN)
    override fun logNext() = log(UsageLogCode94.Action.NEXT)

    private fun log(action: UsageLogCode94.Action) {
        usageLogRepository?.enqueue(
            UsageLogCode94(
                originStr = origin,
                screen = UsageLogCode94.Screen.M2W_INTRO,
                action = action
            )
        )
    }
}