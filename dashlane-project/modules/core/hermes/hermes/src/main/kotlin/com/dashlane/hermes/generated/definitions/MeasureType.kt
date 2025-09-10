package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
enum class MeasureType(override val code: String) : TrackingLog.Enum {
    CPU(
        code = "cpu",
    ),
    DURATION(
        code = "duration",
    ),
    MEMORY(
        code = "memory",
    ),
}