package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
enum class DashboardAction(override val code: String) : TrackingLog.Enum {
    COPY_INVITE(
        code = "copy_invite",
    ),
    REMOVE_MEMBER(
        code = "remove_member",
    ),
    RESET_INVITE(
        code = "reset_invite",
    ),
}