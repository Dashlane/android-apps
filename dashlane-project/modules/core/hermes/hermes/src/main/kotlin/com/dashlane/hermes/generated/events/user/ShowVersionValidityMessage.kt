package com.dashlane.hermes.generated.events.user

import com.dashlane.hermes.TrackingLog
import com.dashlane.hermes.generated.definitions.VersionValidityStatus
import javax.annotation.processing.Generated

@Generated
data class ShowVersionValidityMessage(
    val isUpdatePossible: Boolean,
    val versionValidityStatus: VersionValidityStatus,
) : TrackingLog {
    override val name
        get() = "show_version_validity_message"

    override val category
        get() = TrackingLog.Category.USER

    override val priority
        get() = false

    override fun collect(collector: TrackingLog.Collector) {
        collector["name"] = name
        collector["is_update_possible"] = isUpdatePossible
        collector["version_validity_status"] = versionValidityStatus
    }
}