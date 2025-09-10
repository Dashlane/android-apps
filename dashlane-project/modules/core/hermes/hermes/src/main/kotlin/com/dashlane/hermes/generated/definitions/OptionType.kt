package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
enum class OptionType(override val code: String) : TrackingLog.Enum {
    LOGIN(
        code = "login",
    ),
    SECRET(
        code = "secret",
    ),
    SECURE_NOTE(
        code = "secure_note",
    ),
    USER(
        code = "user",
    ),
    USER_GROUP(
        code = "user_group",
    ),
}