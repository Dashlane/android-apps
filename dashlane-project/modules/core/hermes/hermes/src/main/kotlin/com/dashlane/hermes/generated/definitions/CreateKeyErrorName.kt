package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
enum class CreateKeyErrorName(override val code: String) : TrackingLog.Enum {
    UNKNOWN(
        code = "unknown",
    ),
    WRONG_CONFIRMATION_KEY(
        code = "wrong_confirmation_key",
    ),
}