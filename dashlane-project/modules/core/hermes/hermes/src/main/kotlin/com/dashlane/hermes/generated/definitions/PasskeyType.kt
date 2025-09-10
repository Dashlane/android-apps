package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
enum class PasskeyType(override val code: String) : TrackingLog.Enum {
    CLOUD(
        code = "cloud",
    ),
    LEGACY(
        code = "legacy",
    ),
}