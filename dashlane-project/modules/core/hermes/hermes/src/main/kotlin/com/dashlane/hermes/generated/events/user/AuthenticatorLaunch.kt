package com.dashlane.hermes.generated.events.user

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
data class AuthenticatorLaunch(
    val hasPasswordManagerInstalled: Boolean,
    val isFirstLaunch: Boolean,
) : TrackingLog {
    override val name
        get() = "authenticator_launch"

    override val category
        get() = TrackingLog.Category.USER

    override val priority
        get() = true

    override fun collect(collector: TrackingLog.Collector) {
        collector["name"] = name
        collector["has_password_manager_installed"] = hasPasswordManagerInstalled
        collector["is_first_launch"] = isFirstLaunch
    }
}