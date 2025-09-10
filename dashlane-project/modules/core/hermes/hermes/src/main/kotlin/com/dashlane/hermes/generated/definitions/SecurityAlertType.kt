package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
enum class SecurityAlertType(override val code: String) : TrackingLog.Enum {
    DARK_WEB(
        code = "dark_web",
    ),
    NUDGE_DASHLANE_EXTENSION(
        code = "nudge_dashlane_extension",
    ),
    NUDGE_IN_CONTEXT(
        code = "nudge_in_context",
    ),
    PUBLIC_BREACH(
        code = "public_breach",
    ),
}