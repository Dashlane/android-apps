package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
enum class AuthenticationStatus(override val code: String) : TrackingLog.Enum {
    LOCKED_OUT(
        code = "locked_out",
    ),
    LOGGED_IN(
        code = "logged_in",
    ),
    LOGGED_OUT(
        code = "logged_out",
    ),
}