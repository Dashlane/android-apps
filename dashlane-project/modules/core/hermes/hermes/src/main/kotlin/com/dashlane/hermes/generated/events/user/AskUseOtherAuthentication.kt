package com.dashlane.hermes.generated.events.user

import com.dashlane.hermes.TrackingLog
import com.dashlane.hermes.generated.definitions.Mode
import javax.annotation.processing.Generated

@Generated
data class AskUseOtherAuthentication(
    val next: Mode,
    val previous: Mode,
) : TrackingLog {
    override val name
        get() = "ask_use_other_authentication"

    override val category
        get() = TrackingLog.Category.USER

    override val priority
        get() = true

    override fun collect(collector: TrackingLog.Collector) {
        collector["name"] = name
        collector["next"] = next
        collector["previous"] = previous
    }
}