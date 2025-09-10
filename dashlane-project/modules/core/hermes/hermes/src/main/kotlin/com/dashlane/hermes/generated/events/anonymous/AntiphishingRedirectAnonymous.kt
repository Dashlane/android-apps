package com.dashlane.hermes.generated.events.anonymous

import com.dashlane.hermes.TrackingLog
import com.dashlane.hermes.generated.definitions.Domain
import javax.annotation.processing.Generated

@Generated
data class AntiphishingRedirectAnonymous(
    val redirectDomain: Domain,
    val phishingDomain: Domain,
) : TrackingLog {
    override val name
        get() = "antiphishing_redirect"

    override val category
        get() = TrackingLog.Category.ANONYMOUS

    override val priority
        get() = false

    override fun collect(collector: TrackingLog.Collector) {
        collector["name"] = name
        collector["redirect_domain"] = redirectDomain
        collector["phishing_domain"] = phishingDomain
    }
}