package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
enum class AuthenticatorPushStatus(override val code: String) : TrackingLog.Enum {
    ACCEPTED(
        code = "accepted",
    ),
    RECEIVED(
        code = "received",
    ),
    REJECTED(
        code = "rejected",
    ),
}