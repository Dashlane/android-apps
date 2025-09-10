package com.dashlane.hermes.generated.events.user

import com.dashlane.hermes.TrackingLog
import com.dashlane.hermes.generated.definitions.CeremonyStatus
import com.dashlane.hermes.generated.definitions.PasskeyRegistrationErrorType
import com.dashlane.hermes.generated.definitions.PasskeyType
import javax.annotation.processing.Generated

@Generated
data class RegisterPasskey(
    val passkeyRegistrationErrorType: PasskeyRegistrationErrorType? = null,
    val passkeyType: PasskeyType? = null,
    val passkeyRegistrationStatus: CeremonyStatus,
) : TrackingLog {
    override val name
        get() = "register_passkey"

    override val category
        get() = TrackingLog.Category.USER

    override val priority
        get() = false

    override fun collect(collector: TrackingLog.Collector) {
        collector["name"] = name
        collector["passkey_registration_error_type"] = passkeyRegistrationErrorType
        collector["passkey_type"] = passkeyType
        collector["passkey_registration_status"] = passkeyRegistrationStatus
    }
}