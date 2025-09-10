package com.dashlane.hermes.generated.events.user

import com.dashlane.hermes.TrackingLog
import com.dashlane.hermes.generated.definitions.Space
import javax.annotation.processing.Generated

@Generated
data class SelectSpace(
    val space: Space,
) : TrackingLog {
    override val name
        get() = "select_space"

    override val category
        get() = TrackingLog.Category.USER

    override val priority
        get() = false

    override fun collect(collector: TrackingLog.Collector) {
        collector["name"] = name
        collector["space"] = space
    }
}