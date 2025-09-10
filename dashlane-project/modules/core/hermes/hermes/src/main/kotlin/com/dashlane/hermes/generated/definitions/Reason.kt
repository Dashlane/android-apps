package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
enum class Reason(override val code: String) : TrackingLog.Enum {
    CHANGE_MASTER_PASSWORD(
        code = "change_master_password",
    ),
    EDIT_SETTINGS(
        code = "edit_settings",
    ),
    LOGIN(
        code = "login",
    ),
    UNLOCK_APP(
        code = "unlock_app",
    ),
    UNLOCK_ITEM(
        code = "unlock_item",
    ),
}