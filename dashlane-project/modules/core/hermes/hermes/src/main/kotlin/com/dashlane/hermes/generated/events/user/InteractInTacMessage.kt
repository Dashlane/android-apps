package com.dashlane.hermes.generated.events.user

import com.dashlane.hermes.TrackingLog
import com.dashlane.hermes.generated.definitions.TacNotification
import javax.annotation.processing.Generated

@Generated
data class InteractInTacMessage(
    val isDiscarded: Boolean,
    val hasCta: Boolean,
    val hasClickedCta: Boolean,
    val tacNotification: TacNotification,
) : TrackingLog {
    override val name
        get() = "interact_in_tac_message"

    override val category
        get() = TrackingLog.Category.USER

    override val priority
        get() = false

    override fun collect(collector: TrackingLog.Collector) {
        collector["name"] = name
        collector["is_discarded"] = isDiscarded
        collector["has_cta"] = hasCta
        collector["has_clicked_cta"] = hasClickedCta
        collector["tac_notification"] = tacNotification
    }
}