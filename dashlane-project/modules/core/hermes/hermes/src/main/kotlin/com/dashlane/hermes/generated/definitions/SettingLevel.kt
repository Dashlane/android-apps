package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
enum class SettingLevel(override val code: String) : TrackingLog.Enum {
    CREDENTIALS(
        code = "credentials",
    ),
    GLOBAL(
        code = "global",
    ),
    IDS(
        code = "ids",
    ),
    PAYMENTS(
        code = "payments",
    ),
    SECURE_NOTES(
        code = "secure_notes",
    ),
}