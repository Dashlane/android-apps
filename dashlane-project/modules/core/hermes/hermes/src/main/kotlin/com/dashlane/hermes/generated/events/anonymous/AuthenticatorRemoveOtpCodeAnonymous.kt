package com.dashlane.hermes.generated.events.anonymous

import com.dashlane.hermes.Sha256Hash
import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
data class AuthenticatorRemoveOtpCodeAnonymous(
    val authenticatorIssuerId: Sha256Hash? = null,
) : TrackingLog {
    override val name
        get() = "authenticator_remove_otp_code"

    override val category
        get() = TrackingLog.Category.ANONYMOUS

    override val priority
        get() = false

    override fun collect(collector: TrackingLog.Collector) {
        collector["name"] = name
        collector["authenticator_issuer_id"] = authenticatorIssuerId
    }
}