package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
enum class Rights(override val code: String) : TrackingLog.Enum {
    LIMITED(
        code = "limited",
    ),
    REVOKED(
        code = "revoked",
    ),
    UNLIMITED(
        code = "unlimited",
    ),
}