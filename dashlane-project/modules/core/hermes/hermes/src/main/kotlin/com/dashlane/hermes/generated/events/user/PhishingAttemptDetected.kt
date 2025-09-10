package com.dashlane.hermes.generated.events.user

import com.dashlane.hermes.TrackingLog
import com.dashlane.hermes.generated.definitions.FormType
import javax.annotation.processing.Generated

@Generated
data class PhishingAttemptDetected(
    val confidenceInterval: Int? = null,
    val formType: FormType,
) : TrackingLog {
    override val name
        get() = "phishing_attempt_detected"

    override val category
        get() = TrackingLog.Category.USER

    override val priority
        get() = false

    override fun collect(collector: TrackingLog.Collector) {
        collector["name"] = name
        collector["confidence_interval"] = confidenceInterval
        collector["form_type"] = formType
    }
}