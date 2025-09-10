package com.dashlane.hermes.generated.events.anonymous

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
class PingAnonymous : TrackingLog {
    override val name
        get() = "ping"

    override val category
        get() = TrackingLog.Category.ANONYMOUS

    override val priority
        get() = false

    override fun collect(collector: TrackingLog.Collector) {
        collector["name"] = name
    }
}