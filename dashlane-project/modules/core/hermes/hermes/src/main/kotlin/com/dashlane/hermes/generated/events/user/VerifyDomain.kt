package com.dashlane.hermes.generated.events.user

import com.dashlane.hermes.TrackingLog
import com.dashlane.hermes.generated.definitions.DomainVerificationStep
import javax.annotation.processing.Generated

@Generated
data class VerifyDomain(
    val domainVerificationStep: DomainVerificationStep,
) : TrackingLog {
    override val name
        get() = "verify_domain"

    override val category
        get() = TrackingLog.Category.USER

    override val priority
        get() = false

    override fun collect(collector: TrackingLog.Collector) {
        collector["name"] = name
        collector["domain_verification_step"] = domainVerificationStep
    }
}