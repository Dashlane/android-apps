package com.dashlane.hermes.generated.events.user

import com.dashlane.hermes.TrackingLog
import com.dashlane.hermes.generated.definitions.Mode
import com.dashlane.hermes.generated.definitions.Reason
import com.dashlane.hermes.generated.definitions.VerificationMode
import javax.annotation.processing.Generated

@Generated
data class AskAuthentication(
    val mode: Mode,
    val reason: Reason,
    val verificationMode: VerificationMode? = null,
) : TrackingLog {
    override val name
        get() = "ask_authentication"

    override val category
        get() = TrackingLog.Category.USER

    override val priority
        get() = true

    override fun collect(collector: TrackingLog.Collector) {
        collector["name"] = name
        collector["mode"] = mode
        collector["reason"] = reason
        collector["verification_mode"] = verificationMode
    }
}