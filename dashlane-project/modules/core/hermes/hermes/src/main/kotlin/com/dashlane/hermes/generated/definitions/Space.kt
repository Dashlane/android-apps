package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
enum class Space(override val code: String) : TrackingLog.Enum {
    ALL(
        code = "all",
    ),
    PERSONAL(
        code = "personal",
    ),
    PROFESSIONAL(
        code = "professional",
    ),
}