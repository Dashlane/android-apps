package com.dashlane.hermes.generated.events.user

import com.dashlane.hermes.TrackingLog
import com.dashlane.hermes.generated.definitions.BusinessDomain
import java.time.ZonedDateTime
import javax.annotation.processing.Generated

@Suppress("kotlin:S1133")
@Deprecated("")
@Generated
data class Mv3ExtensionTaskReport(
    val taskName: String,
    val feature: String,
    val hasWokenUpServiceWorker: Boolean? = null,
    val businessDomain: BusinessDomain,
    val serviceWorkerInterruptionCount: Int,
    val taskEndDateTime: ZonedDateTime,
    val serviceWorkerStartDateTime: ZonedDateTime,
    val taskStartDateTime: ZonedDateTime,
) : TrackingLog {
    override val name
        get() = "mv3_extension_task_report"

    override val category
        get() = TrackingLog.Category.USER

    override val priority
        get() = false

    override fun collect(collector: TrackingLog.Collector) {
        collector["name"] = name
        collector["task_name"] = taskName
        collector["feature"] = feature
        collector["has_woken_up_service_worker"] = hasWokenUpServiceWorker
        collector["business_domain"] = businessDomain
        collector["service_worker_interruption_count"] = serviceWorkerInterruptionCount
        collector["task_end_date_time"] = taskEndDateTime
        collector["service_worker_start_date_time"] = serviceWorkerStartDateTime
        collector["task_start_date_time"] = taskStartDateTime
    }
}