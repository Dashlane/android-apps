package com.dashlane.hermes.generated.events.user

import com.dashlane.hermes.TrackingLog
import com.dashlane.hermes.generated.definitions.DismissType
import javax.annotation.processing.Generated

@Generated
data class AutofillDismiss(
    val dismissType: DismissType,
) : TrackingLog {
    override val name
        get() = "autofill_dismiss"

    override val category
        get() = TrackingLog.Category.USER

    override val priority
        get() = false

    override fun collect(collector: TrackingLog.Collector) {
        collector["name"] = name
        collector["dismiss_type"] = dismissType
    }
}