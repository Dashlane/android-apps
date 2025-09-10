package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
enum class UpdateStatus(override val code: String) : TrackingLog.Enum {
    ERROR(
        code = "error",
    ),
    UPDATED(
        code = "updated",
    ),
}