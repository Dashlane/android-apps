package com.dashlane.hermes.generated.events.user

import com.dashlane.hermes.TrackingLog
import com.dashlane.hermes.generated.definitions.OtpAdditionMode
import javax.annotation.processing.Generated

@Generated
data class AuthenticatorAddOtpCode(
    val otpAdditionMode: OtpAdditionMode,
) : TrackingLog {
    override val name
        get() = "authenticator_add_otp_code"

    override val category
        get() = TrackingLog.Category.USER

    override val priority
        get() = false

    override fun collect(collector: TrackingLog.Collector) {
        collector["name"] = name
        collector["otp_addition_mode"] = otpAdditionMode
    }
}