package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
enum class IntegrationPlatform(override val code: String) : TrackingLog.Enum {
    DASHLANE_EXTENSION(
        code = "dashlane_extension",
    ),
    GLOBAL(
        code = "global",
    ),
    IN_CONTEXT(
        code = "in_context",
    ),
    SLACK(
        code = "slack",
    ),
}