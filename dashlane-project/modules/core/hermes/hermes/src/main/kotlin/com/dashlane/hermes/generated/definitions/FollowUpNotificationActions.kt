package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
enum class FollowUpNotificationActions(override val code: String) : TrackingLog.Enum {
    ACTIVATE_FEATURE(
        code = "activate_feature",
    ),
    COPY(
        code = "copy",
    ),
    DEACTIVATE_FEATURE(
        code = "deactivate_feature",
    ),
    DISMISS(
        code = "dismiss",
    ),
    TRIGGER(
        code = "trigger",
    ),
}