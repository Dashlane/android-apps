package com.dashlane.hermes.generated.events.user

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
class LoadDarkWebInsightsResults : TrackingLog {
    override val name
        get() = "load_dark_web_insights_results"

    override val category
        get() = TrackingLog.Category.USER

    override val priority
        get() = false

    override fun collect(collector: TrackingLog.Collector) {
        collector["name"] = name
    }
}