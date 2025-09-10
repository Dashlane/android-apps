package com.dashlane.hermes.generated.events.user

import com.dashlane.hermes.TrackingLog
import com.dashlane.hermes.generated.definitions.AccessPath
import javax.annotation.processing.Generated

@Generated
data class OpenDomainDarkWebMonitoring(
    val accessPath: AccessPath,
    val isFirstVisit: Boolean,
) : TrackingLog {
    override val name
        get() = "open_domain_dark_web_monitoring"

    override val category
        get() = TrackingLog.Category.USER

    override val priority
        get() = false

    override fun collect(collector: TrackingLog.Collector) {
        collector["name"] = name
        collector["access_path"] = accessPath
        collector["is_first_visit"] = isFirstVisit
    }
}