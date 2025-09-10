package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
enum class UseKeyErrorName(override val code: String) : TrackingLog.Enum {
    UNKNOWN(
        code = "unknown",
    ),
    WRONG_KEY_ENTERED(
        code = "wrong_key_entered",
    ),
}