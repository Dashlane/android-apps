package com.dashlane.hermes.generated.events.user

import com.dashlane.hermes.TrackingLog
import com.dashlane.hermes.generated.definitions.BusinessDomain
import java.time.ZonedDateTime
import javax.annotation.processing.Generated

@Suppress("kotlin:S1133")
@Deprecated("")
@Generated
data class Mv3ExtensionTaskInterruption(
    val taskName: String,
    val feature: String,
    val businessDomain: BusinessDomain,
    val serviceWorkerStartDateTime: ZonedDateTime,
    val taskStartDateTime: ZonedDateTime,
) : TrackingLog {
    override val name
        get() = "mv3_extension_task_interruption"

    override val category
        get() = TrackingLog.Category.USER

    override val priority
        get() = false

    override fun collect(collector: TrackingLog.Collector) {
        collector["name"] = name
        collector["task_name"] = taskName
        collector["feature"] = feature
        collector["business_domain"] = businessDomain
        collector["service_worker_start_date_time"] = serviceWorkerStartDateTime
        collector["task_start_date_time"] = taskStartDateTime
    }
}