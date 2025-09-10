package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
enum class ChangeLoginError(override val code: String) : TrackingLog.Enum {
    EMAIL_IN_PENDING_REQUEST(
        code = "email_in_pending_request",
    ),
    EXISTING_EMAIL(
        code = "existing_email",
    ),
    GENERIC(
        code = "generic",
    ),
    INVALID_DOMAIN(
        code = "invalid_domain",
    ),
    INVALID_EMAIL(
        code = "invalid_email",
    ),
    SAME_EMAIL(
        code = "same_email",
    ),
    WRONG_TOKEN(
        code = "wrong_token",
    ),
}