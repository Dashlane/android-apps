package com.dashlane.hermes.generated.events.user

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
data class ForgetMasterPassword(
    val hasTeamAccountRecovery: Boolean,
    val hasBiometricReset: Boolean,
) : TrackingLog {
    override val name
        get() = "forget_master_password"

    override val category
        get() = TrackingLog.Category.USER

    override val priority
        get() = true

    override fun collect(collector: TrackingLog.Collector) {
        collector["name"] = name
        collector["has_team_account_recovery"] = hasTeamAccountRecovery
        collector["has_biometric_reset"] = hasBiometricReset
    }
}