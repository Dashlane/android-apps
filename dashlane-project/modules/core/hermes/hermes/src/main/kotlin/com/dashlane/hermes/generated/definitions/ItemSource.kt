package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
enum class ItemSource(override val code: String) : TrackingLog.Enum {
    MANUAL(
        code = "manual",
    ),
    SHARED(
        code = "shared",
    ),
}