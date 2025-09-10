package com.dashlane.hermes.generated.events.anonymous

import com.dashlane.hermes.Sha256Hash
import com.dashlane.hermes.TrackingLog
import com.dashlane.hermes.generated.definitions.Domain
import com.dashlane.hermes.generated.definitions.FlowStep
import com.dashlane.hermes.generated.definitions.OtpAdditionError
import com.dashlane.hermes.generated.definitions.OtpAdditionMode
import com.dashlane.hermes.generated.definitions.OtpSpecifications
import com.dashlane.hermes.generated.definitions.Space
import javax.annotation.processing.Generated

@Generated
data class AddTwoFactorAuthenticationToCredentialAnonymous(
    val otpSpecifications: OtpSpecifications? = null,
    val otpAdditionError: OtpAdditionError? = null,
    val flowStep: FlowStep,
    val domain: Domain? = null,
    val authenticatorIssuerId: Sha256Hash? = null,
    val otpAdditionMode: OtpAdditionMode,
    val space: Space? = null,
) : TrackingLog {
    override val name
        get() = "add_two_factor_authentication_to_credential"

    override val category
        get() = TrackingLog.Category.ANONYMOUS

    override val priority
        get() = false

    override fun collect(collector: TrackingLog.Collector) {
        collector["name"] = name
        collector["otp_specifications"] = otpSpecifications
        collector["otp_addition_error"] = otpAdditionError
        collector["flow_step"] = flowStep
        collector["domain"] = domain
        collector["authenticator_issuer_id"] = authenticatorIssuerId
        collector["otp_addition_mode"] = otpAdditionMode
        collector["space"] = space
    }
}