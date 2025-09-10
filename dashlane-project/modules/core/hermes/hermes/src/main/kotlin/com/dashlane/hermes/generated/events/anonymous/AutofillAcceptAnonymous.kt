package com.dashlane.hermes.generated.events.anonymous

import com.dashlane.hermes.TrackingLog
import com.dashlane.hermes.generated.definitions.Domain
import com.dashlane.hermes.generated.definitions.PhishingRisk
import com.dashlane.hermes.generated.definitions.WebcardSaveOptions
import javax.annotation.processing.Generated

@Generated
data class AutofillAcceptAnonymous(
    val phishingRisk: PhishingRisk? = null,
    val itemPosition: Int? = null,
    val isSetAsDefault: Boolean? = null,
    val domain: Domain,
    val webcardOptionSelected: WebcardSaveOptions? = null,
    val isProtected: Boolean? = null,
) : TrackingLog {
    override val name
        get() = "autofill_accept"

    override val category
        get() = TrackingLog.Category.ANONYMOUS

    override val priority
        get() = true

    override fun collect(collector: TrackingLog.Collector) {
        collector["name"] = name
        collector["phishing_risk"] = phishingRisk
        collector["item_position"] = itemPosition
        collector["is_set_as_default"] = isSetAsDefault
        collector["domain"] = domain
        collector["webcard_option_selected"] = webcardOptionSelected
        collector["is_protected"] = isProtected
    }
}