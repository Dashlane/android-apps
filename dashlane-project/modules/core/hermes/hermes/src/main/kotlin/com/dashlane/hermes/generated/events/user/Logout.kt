package com.dashlane.hermes.generated.events.user

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
class Logout : TrackingLog {
    override val name
        get() = "logout"

    override val category
        get() = TrackingLog.Category.USER

    override val priority
        get() = true

    override fun collect(collector: TrackingLog.Collector) {
        collector["name"] = name
    }
}