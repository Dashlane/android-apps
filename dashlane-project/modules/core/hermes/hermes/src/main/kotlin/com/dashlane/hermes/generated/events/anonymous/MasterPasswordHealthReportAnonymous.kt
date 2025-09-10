package com.dashlane.hermes.generated.events.anonymous

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
data class MasterPasswordHealthReportAnonymous(
    val isWeak: Boolean? = null,
    val isLeaked: Boolean? = null,
) : TrackingLog {
    override val name
        get() = "master_password_health_report"

    override val category
        get() = TrackingLog.Category.ANONYMOUS

    override val priority
        get() = false

    override fun collect(collector: TrackingLog.Collector) {
        collector["name"] = name
        collector["is_weak"] = isWeak
        collector["is_leaked"] = isLeaked
    }
}