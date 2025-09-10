package com.dashlane.hermes.generated.events.user

import com.dashlane.hermes.TrackingLog
import com.dashlane.hermes.generated.definitions.AuthenticationStatus
import javax.annotation.processing.Generated

@Generated
data class OpenExtension(
    val authenticationStatus: AuthenticationStatus? = null,
) : TrackingLog {
    override val name
        get() = "open_extension"

    override val category
        get() = TrackingLog.Category.USER

    override val priority
        get() = false

    override fun collect(collector: TrackingLog.Collector) {
        collector["name"] = name
        collector["authentication_status"] = authenticationStatus
    }
}