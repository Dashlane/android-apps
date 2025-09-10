package com.dashlane.hermes.generated.events.user

import com.dashlane.hermes.TrackingLog
import com.dashlane.hermes.generated.definitions.FlowStep
import com.dashlane.hermes.generated.definitions.TwoFactorAuthenticationError
import javax.annotation.processing.Generated

@Generated
data class DeleteU2fAuthenticator(
    val errorName: TwoFactorAuthenticationError? = null,
    val flowStep: FlowStep,
) : TrackingLog {
    override val name
        get() = "delete_u2f_authenticator"

    override val category
        get() = TrackingLog.Category.USER

    override val priority
        get() = false

    override fun collect(collector: TrackingLog.Collector) {
        collector["name"] = name
        collector["error_name"] = errorName
        collector["flow_step"] = flowStep
    }
}