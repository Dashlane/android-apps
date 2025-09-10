package com.dashlane.hermes.generated.events.user

import com.dashlane.hermes.TrackingLog
import com.dashlane.hermes.generated.definitions.Duration
import com.dashlane.hermes.generated.definitions.Error
import com.dashlane.hermes.generated.definitions.ErrorDescription
import com.dashlane.hermes.generated.definitions.ErrorName
import com.dashlane.hermes.generated.definitions.ErrorStep
import com.dashlane.hermes.generated.definitions.Extent
import com.dashlane.hermes.generated.definitions.TreatProblem
import com.dashlane.hermes.generated.definitions.Trigger
import javax.annotation.processing.Generated

@Suppress("kotlin:S1133")
@Deprecated("")
@Generated
data class Sync(
    val extent: Extent,
    val outgoingUpdateCount: Int? = null,
    val deduplicates: Int? = null,
    val errorName: ErrorName? = null,
    val outgoingDeleteCount: Int? = null,
    val errorStep: ErrorStep? = null,
    val incomingDeleteCount: Int? = null,
    val incomingUpdateCount: Int? = null,
    val itemGroupId: String? = null,
    val fullBackupSize: Int? = null,
    val trigger: Trigger,
    val error: Error? = null,
    val duration: Duration,
    val errorDescription: ErrorDescription? = null,
    val treatProblem: TreatProblem? = null,
    val timestamp: Int,
) : TrackingLog {
    override val name
        get() = "sync"

    override val category
        get() = TrackingLog.Category.USER

    override val priority
        get() = false

    override fun collect(collector: TrackingLog.Collector) {
        collector["name"] = name
        collector["extent"] = extent
        collector["outgoing_update_count"] = outgoingUpdateCount
        collector["deduplicates"] = deduplicates
        collector["error_name"] = errorName
        collector["outgoing_delete_count"] = outgoingDeleteCount
        collector["error_step"] = errorStep
        collector["incoming_delete_count"] = incomingDeleteCount
        collector["incoming_update_count"] = incomingUpdateCount
        collector["item_group_id"] = itemGroupId
        collector["full_backup_size"] = fullBackupSize
        collector["trigger"] = trigger
        collector["error"] = error
        collector["duration"] = duration
        collector["error_description"] = errorDescription
        collector["treat_problem"] = treatProblem
        collector["timestamp"] = timestamp
    }
}