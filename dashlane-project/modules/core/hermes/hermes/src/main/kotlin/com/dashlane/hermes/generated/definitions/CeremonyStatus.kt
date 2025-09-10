package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
enum class CeremonyStatus(override val code: String) : TrackingLog.Enum {
    CANCELLED(
        code = "cancelled",
    ),
    FAILURE(
        code = "failure",
    ),
    SUCCESS(
        code = "success",
    ),
}