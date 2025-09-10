package com.dashlane.hermes.generated.events.user

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
data class PasswordManagerLaunch(
    val authenticatorOtpCodesCount: Int,
    val hasAuthenticatorInstalled: Boolean,
    val isFirstLaunch: Boolean,
) : TrackingLog {
    override val name
        get() = "password_manager_launch"

    override val category
        get() = TrackingLog.Category.USER

    override val priority
        get() = false

    override fun collect(collector: TrackingLog.Collector) {
        collector["name"] = name
        collector["authenticator_otp_codes_count"] = authenticatorOtpCodesCount
        collector["has_authenticator_installed"] = hasAuthenticatorInstalled
        collector["is_first_launch"] = isFirstLaunch
    }
}