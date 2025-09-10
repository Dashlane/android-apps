package com.dashlane.hermes.generated.events.user

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Suppress("kotlin:S1133")
@Deprecated("")
@Generated
data class LaunchPasswordChanger(
    val isBulkChange: Boolean,
    val bulkChangeCredentialCount: Int? = null,
    val isSuccess: Boolean,
) : TrackingLog {
    override val name
        get() = "launch_password_changer"

    override val category
        get() = TrackingLog.Category.USER

    override val priority
        get() = false

    override fun collect(collector: TrackingLog.Collector) {
        collector["name"] = name
        collector["is_bulk_change"] = isBulkChange
        collector["bulk_change_credential_count"] = bulkChangeCredentialCount
        collector["is_success"] = isSuccess
    }
}