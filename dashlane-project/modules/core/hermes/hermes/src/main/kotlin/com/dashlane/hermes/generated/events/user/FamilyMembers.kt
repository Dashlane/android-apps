package com.dashlane.hermes.generated.events.user

import com.dashlane.hermes.TrackingLog
import com.dashlane.hermes.generated.definitions.DashboardAction
import javax.annotation.processing.Generated

@Generated
data class FamilyMembers(
    val dashboardAction: DashboardAction,
) : TrackingLog {
    override val name
        get() = "family_members"

    override val category
        get() = TrackingLog.Category.USER

    override val priority
        get() = true

    override fun collect(collector: TrackingLog.Collector) {
        collector["name"] = name
        collector["dashboard_action"] = dashboardAction
    }
}