package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
enum class OriginalAuthMethod(override val code: String) : TrackingLog.Enum {
    MP(
        code = "mp",
    ),
    MPLESS(
        code = "mpless",
    ),
    PROPERTY_NOT_AVAILABLE(
        code = "property_not_available",
    ),
}