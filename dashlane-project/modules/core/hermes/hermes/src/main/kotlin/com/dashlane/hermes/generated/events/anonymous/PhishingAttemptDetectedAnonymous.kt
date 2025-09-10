package com.dashlane.hermes.generated.events.anonymous

import com.dashlane.hermes.TrackingLog
import com.dashlane.hermes.generated.definitions.Domain
import com.dashlane.hermes.generated.definitions.FormType
import javax.annotation.processing.Generated

@Generated
data class PhishingAttemptDetectedAnonymous(
    val confidence: Double,
    val domain: Domain,
    val formType: FormType,
) : TrackingLog {
    override val name
        get() = "phishing_attempt_detected"

    override val category
        get() = TrackingLog.Category.ANONYMOUS

    override val priority
        get() = false

    override fun collect(collector: TrackingLog.Collector) {
        collector["name"] = name
        collector["confidence"] = confidence
        collector["domain"] = domain
        collector["form_type"] = formType
    }
}