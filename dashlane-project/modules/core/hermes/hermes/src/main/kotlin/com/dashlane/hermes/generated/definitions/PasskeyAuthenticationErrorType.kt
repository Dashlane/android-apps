package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
enum class PasskeyAuthenticationErrorType(override val code: String) : TrackingLog.Enum {
    INVALID_STATE(
        code = "invalid_state",
    ),
    NOT_ALLOWED(
        code = "not_allowed",
    ),
    NOT_SUPPORTED(
        code = "not_supported",
    ),
    SECURITY(
        code = "security",
    ),
    UNKNOWN(
        code = "unknown",
    ),
}