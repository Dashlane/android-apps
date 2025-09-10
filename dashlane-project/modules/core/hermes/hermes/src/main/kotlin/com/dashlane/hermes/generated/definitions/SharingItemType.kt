package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
enum class SharingItemType(override val code: String) : TrackingLog.Enum {
    CREDENTIAL(
        code = "credential",
    ),
    SECRET(
        code = "secret",
    ),
    SECURE_NOTE(
        code = "secure_note",
    ),
}