package com.dashlane.hermes.generated.events.user

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
data class ToggleAnalytics(
    val isAnalyticsEnabled: Boolean,
) : TrackingLog {
    override val name
        get() = "toggle_analytics"

    override val category
        get() = TrackingLog.Category.USER

    override val priority
        get() = false

    override fun collect(collector: TrackingLog.Collector) {
        collector["name"] = name
        collector["is_analytics_enabled"] = isAnalyticsEnabled
    }
}