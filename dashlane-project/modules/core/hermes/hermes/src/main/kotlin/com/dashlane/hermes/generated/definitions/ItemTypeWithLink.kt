package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
enum class ItemTypeWithLink(override val code: String) : TrackingLog.Enum {
    CREDENTIAL(
        code = "credential",
    ),
    PASSKEY(
        code = "passkey",
    ),
}