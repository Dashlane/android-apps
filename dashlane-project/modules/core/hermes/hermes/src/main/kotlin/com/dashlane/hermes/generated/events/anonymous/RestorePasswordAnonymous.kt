package com.dashlane.hermes.generated.events.anonymous

import com.dashlane.hermes.TrackingLog
import com.dashlane.hermes.generated.definitions.Domain
import javax.annotation.processing.Generated

@Generated
data class RestorePasswordAnonymous(
    val domain: Domain,
) : TrackingLog {
    override val name
        get() = "restore_password"

    override val category
        get() = TrackingLog.Category.ANONYMOUS

    override val priority
        get() = false

    override fun collect(collector: TrackingLog.Collector) {
        collector["name"] = name
        collector["domain"] = domain
    }
}