package com.dashlane.hermes.generated.events.user

import com.dashlane.hermes.TrackingLog
import com.dashlane.hermes.generated.definitions.CallToAction
import javax.annotation.processing.Generated

@Generated
data class OpenPricingPage(
    val callToAction: CallToAction,
) : TrackingLog {
    override val name
        get() = "open_pricing_page"

    override val category
        get() = TrackingLog.Category.USER

    override val priority
        get() = false

    override fun collect(collector: TrackingLog.Collector) {
        collector["name"] = name
        collector["call_to_action"] = callToAction
    }
}