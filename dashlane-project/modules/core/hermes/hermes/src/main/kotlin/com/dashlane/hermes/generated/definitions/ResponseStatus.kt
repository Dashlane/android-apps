package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
enum class ResponseStatus(override val code: String) : TrackingLog.Enum {
    ACCEPTED(
        code = "accepted",
    ),
    DENIED(
        code = "denied",
    ),
    ERROR(
        code = "error",
    ),
}