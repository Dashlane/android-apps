package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
enum class Scope(override val code: String) : TrackingLog.Enum {
    GLOBAL(
        code = "global",
    ),
    PERSONAL(
        code = "personal",
    ),
    TEAM(
        code = "team",
    ),
}