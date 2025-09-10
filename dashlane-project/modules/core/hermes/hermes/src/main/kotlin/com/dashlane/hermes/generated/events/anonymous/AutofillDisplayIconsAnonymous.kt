package com.dashlane.hermes.generated.events.anonymous

import com.dashlane.hermes.TrackingLog
import com.dashlane.hermes.generated.definitions.Domain
import javax.annotation.processing.Generated

@Generated
data class AutofillDisplayIconsAnonymous(
    val domain: Domain,
    val msToIcon: Int,
) : TrackingLog {
    override val name
        get() = "autofill_display_icons"

    override val category
        get() = TrackingLog.Category.ANONYMOUS

    override val priority
        get() = false

    override fun collect(collector: TrackingLog.Collector) {
        collector["name"] = name
        collector["domain"] = domain
        collector["ms_to_icon"] = msToIcon
    }
}