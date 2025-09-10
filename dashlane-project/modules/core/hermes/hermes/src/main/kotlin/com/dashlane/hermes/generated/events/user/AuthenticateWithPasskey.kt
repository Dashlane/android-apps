package com.dashlane.hermes.generated.events.user

import com.dashlane.hermes.TrackingLog
import com.dashlane.hermes.generated.definitions.CeremonyStatus
import com.dashlane.hermes.generated.definitions.PasskeyAuthenticationErrorType
import com.dashlane.hermes.generated.definitions.PasskeyType
import javax.annotation.processing.Generated

@Generated
data class AuthenticateWithPasskey(
    val passkeyType: PasskeyType? = null,
    val passkeyAuthenticationErrorType: PasskeyAuthenticationErrorType? = null,
    val passkeyAuthenticationStatus: CeremonyStatus,
) : TrackingLog {
    override val name
        get() = "authenticate_with_passkey"

    override val category
        get() = TrackingLog.Category.USER

    override val priority
        get() = false

    override fun collect(collector: TrackingLog.Collector) {
        collector["name"] = name
        collector["passkey_type"] = passkeyType
        collector["passkey_authentication_error_type"] = passkeyAuthenticationErrorType
        collector["passkey_authentication_status"] = passkeyAuthenticationStatus
    }
}