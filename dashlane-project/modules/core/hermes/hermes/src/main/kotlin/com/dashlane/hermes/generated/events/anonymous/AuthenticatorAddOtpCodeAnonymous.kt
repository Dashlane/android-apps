package com.dashlane.hermes.generated.events.anonymous

import com.dashlane.hermes.Sha256Hash
import com.dashlane.hermes.TrackingLog
import com.dashlane.hermes.generated.definitions.OtpAdditionMode
import com.dashlane.hermes.generated.definitions.OtpSpecifications
import javax.annotation.processing.Generated

@Generated
data class AuthenticatorAddOtpCodeAnonymous(
    val otpSpecifications: OtpSpecifications? = null,
    val authenticatorIssuerId: Sha256Hash? = null,
    val otpAdditionMode: OtpAdditionMode,
) : TrackingLog {
    override val name
        get() = "authenticator_add_otp_code"

    override val category
        get() = TrackingLog.Category.ANONYMOUS

    override val priority
        get() = false

    override fun collect(collector: TrackingLog.Collector) {
        collector["name"] = name
        collector["otp_specifications"] = otpSpecifications
        collector["authenticator_issuer_id"] = authenticatorIssuerId
        collector["otp_addition_mode"] = otpAdditionMode
    }
}