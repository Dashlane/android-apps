package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
enum class AutofillOrigin(override val code: String) : TrackingLog.Enum {
    AUTOMATIC(
        code = "automatic",
    ),
    CONTEXT_MENU(
        code = "context_menu",
    ),
    DROPDOWN(
        code = "dropdown",
    ),
    KEYBOARD(
        code = "keyboard",
    ),
    NOTIFICATION(
        code = "notification",
    ),
}