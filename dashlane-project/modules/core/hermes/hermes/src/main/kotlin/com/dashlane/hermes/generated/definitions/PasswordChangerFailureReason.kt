package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
enum class PasswordChangerFailureReason(override val code: String) : TrackingLog.Enum {
    CLIENT_ERROR(
        code = "client_error",
    ),
    IDENTITY_ERROR(
        code = "identity_error",
    ),
    LOGIN_ERROR(
        code = "login_error",
    ),
    NETWORK_ERROR(
        code = "network_error",
    ),
    RECIPE_ERROR(
        code = "recipe_error",
    ),
    SERVER_ERROR(
        code = "server_error",
    ),
    USER_CANCEL(
        code = "user_cancel",
    ),
}