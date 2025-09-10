package com.dashlane.hermes.generated.events.user

import com.dashlane.hermes.TrackingLog
import com.dashlane.hermes.generated.definitions.AuthenticatorUserVerificationSource
import com.dashlane.hermes.generated.definitions.Mode
import javax.annotation.processing.Generated

@Generated
data class UserVerificationAttempted(
    val verificationMethod: Mode,
    val attemptNum: Int,
    val source: AuthenticatorUserVerificationSource,
) : TrackingLog {
    override val name
        get() = "user_verification_attempted"

    override val category
        get() = TrackingLog.Category.USER

    override val priority
        get() = false

    override fun collect(collector: TrackingLog.Collector) {
        collector["name"] = name
        collector["verification_method"] = verificationMethod
        collector["attempt_num"] = attemptNum
        collector["source"] = source
    }
}