package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
enum class Action(override val code: String) : TrackingLog.Enum {
    ADD(
        code = "add",
    ),
    ADD_CUSTOM_FIELD(
        code = "add_custom_field",
    ),
    CONNECT(
        code = "connect",
    ),
    DELETE(
        code = "delete",
    ),
    DELETE_CUSTOM_FIELD(
        code = "delete_custom_field",
    ),
    DISPLAY_QR_CODE(
        code = "display_qr_code",
    ),
    EDIT(
        code = "edit",
    ),
    EDIT_CUSTOM_FIELD(
        code = "edit_custom_field",
    ),
    EXCLUDE_ITEM_FROM_PASSWORD_HEALTH(
        code = "exclude_item_from_password_health",
    ),
}