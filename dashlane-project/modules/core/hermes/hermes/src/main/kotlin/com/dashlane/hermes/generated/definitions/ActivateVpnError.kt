package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
enum class ActivateVpnError(override val code: String) : TrackingLog.Enum {
    EMAIL_ALREADY_IN_USE(
        code = "email_already_in_use",
    ),
    SERVER_ERROR(
        code = "server_error",
    ),
}