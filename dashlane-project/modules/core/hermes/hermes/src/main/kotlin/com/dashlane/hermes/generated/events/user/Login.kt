package com.dashlane.hermes.generated.events.user

import com.dashlane.hermes.TrackingLog
import com.dashlane.hermes.generated.definitions.Mode
import com.dashlane.hermes.generated.definitions.Status
import com.dashlane.hermes.generated.definitions.VerificationMode
import javax.annotation.processing.Generated

@Generated
data class Login(
    val mode: Mode? = null,
    val isFirstLogin: Boolean? = null,
    val isBackupCode: Boolean? = null,
    val verificationMode: VerificationMode? = null,
    val status: Status,
) : TrackingLog {
    override val name
        get() = "login"

    override val category
        get() = TrackingLog.Category.USER

    override val priority
        get() = true

    override fun collect(collector: TrackingLog.Collector) {
        collector["name"] = name
        collector["mode"] = mode
        collector["is_first_login"] = isFirstLogin
        collector["is_backup_code"] = isBackupCode
        collector["verification_mode"] = verificationMode
        collector["status"] = status
    }
}