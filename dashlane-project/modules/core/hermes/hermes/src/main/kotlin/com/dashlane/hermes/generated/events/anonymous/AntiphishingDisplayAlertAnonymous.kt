package com.dashlane.hermes.generated.events.anonymous

import com.dashlane.hermes.TrackingLog
import com.dashlane.hermes.generated.definitions.Domain
import javax.annotation.processing.Generated

@Generated
data class AntiphishingDisplayAlertAnonymous(
    val msToAlert: Int,
    val msToScraping: Int? = null,
    val domain: Domain,
    val msToPrediction: Int? = null,
) : TrackingLog {
    override val name
        get() = "antiphishing_display_alert"

    override val category
        get() = TrackingLog.Category.ANONYMOUS

    override val priority
        get() = false

    override fun collect(collector: TrackingLog.Collector) {
        collector["name"] = name
        collector["ms_to_alert"] = msToAlert
        collector["ms_to_scraping"] = msToScraping
        collector["domain"] = domain
        collector["ms_to_prediction"] = msToPrediction
    }
}