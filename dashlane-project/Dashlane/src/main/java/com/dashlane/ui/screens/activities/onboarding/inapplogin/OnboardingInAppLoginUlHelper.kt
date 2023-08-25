package com.dashlane.ui.screens.activities.onboarding.inapplogin

import com.dashlane.teamspaces.manager.TeamspaceManager
import com.dashlane.useractivity.log.usage.UsageLogCode34
import com.dashlane.useractivity.log.usage.UsageLogCode95
import com.dashlane.useractivity.log.usage.UsageLogRepository

class OnboardingInAppLoginUlHelper(
    private val teamspaceManager: TeamspaceManager?,
    private val usageLogRepository: UsageLogRepository?
) {
    fun sendUsageLog95(action: UsageLogCode95.Action?, from: String?, type: UsageLogCode95.Type?) {
        usageLogRepository?.enqueue(
            UsageLogCode95(
                action = action,
                fromStr = from,
                type = type
            )
        )
    }

    fun sendUsageLog34() {
        usageLogRepository?.enqueue(
            UsageLogCode34(
                spaceId = teamspaceManager?.current?.anonTeamId,
                viewName = "openInAppLoginOnboarding"
            )
        )
    }
}