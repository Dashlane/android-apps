package com.dashlane.hermes.generated.events.user

import com.dashlane.hermes.TrackingLog
import com.dashlane.hermes.generated.definitions.AuthenticatorPushStatus
import com.dashlane.hermes.generated.definitions.AuthenticatorPushType
import javax.annotation.processing.Generated

@Generated
data class AuthenticatorPushAction(
    val authenticatorPushType: AuthenticatorPushType,
    val authenticatorPushStatus: AuthenticatorPushStatus,
) : TrackingLog {
    override val name
        get() = "authenticator_push_action"

    override val category
        get() = TrackingLog.Category.USER

    override val priority
        get() = false

    override fun collect(collector: TrackingLog.Collector) {
        collector["name"] = name
        collector["authenticator_push_type"] = authenticatorPushType
        collector["authenticator_push_status"] = authenticatorPushStatus
    }
}