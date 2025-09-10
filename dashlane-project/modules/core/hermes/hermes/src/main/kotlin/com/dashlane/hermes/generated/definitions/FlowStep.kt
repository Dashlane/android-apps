package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
enum class FlowStep(override val code: String) : TrackingLog.Enum {
    CANCEL(
        code = "cancel",
    ),
    COMPLETE(
        code = "complete",
    ),
    ERROR(
        code = "error",
    ),
    START(
        code = "start",
    ),
}