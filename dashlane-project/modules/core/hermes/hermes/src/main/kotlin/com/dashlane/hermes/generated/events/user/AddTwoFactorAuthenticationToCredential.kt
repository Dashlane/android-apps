package com.dashlane.hermes.generated.events.user

import com.dashlane.hermes.TrackingLog
import com.dashlane.hermes.generated.definitions.FlowStep
import com.dashlane.hermes.generated.definitions.ItemId
import com.dashlane.hermes.generated.definitions.OtpAdditionError
import com.dashlane.hermes.generated.definitions.OtpAdditionMode
import com.dashlane.hermes.generated.definitions.Space
import javax.annotation.processing.Generated

@Generated
data class AddTwoFactorAuthenticationToCredential(
    val otpAdditionError: OtpAdditionError? = null,
    val flowStep: FlowStep,
    val itemId: ItemId? = null,
    val otpAdditionMode: OtpAdditionMode,
    val space: Space? = null,
) : TrackingLog {
    override val name
        get() = "add_two_factor_authentication_to_credential"

    override val category
        get() = TrackingLog.Category.USER

    override val priority
        get() = false

    override fun collect(collector: TrackingLog.Collector) {
        collector["name"] = name
        collector["otp_addition_error"] = otpAdditionError
        collector["flow_step"] = flowStep
        collector["item_id"] = itemId
        collector["otp_addition_mode"] = otpAdditionMode
        collector["space"] = space
    }
}