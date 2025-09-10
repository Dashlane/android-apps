package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
enum class PhishingRisk(override val code: String) : TrackingLog.Enum {
    HIGH(
        code = "high",
    ),
    MODERATE(
        code = "moderate",
    ),
    NONE(
        code = "none",
    ),
}