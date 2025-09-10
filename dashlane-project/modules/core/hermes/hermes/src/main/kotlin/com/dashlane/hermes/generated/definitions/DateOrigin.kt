package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
enum class DateOrigin(override val code: String) : TrackingLog.Enum {
    LOCAL(
        code = "local",
    ),
    REMOTE(
        code = "remote",
    ),
}