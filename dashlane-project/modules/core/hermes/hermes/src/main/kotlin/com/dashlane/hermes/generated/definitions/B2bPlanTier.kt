package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
enum class B2bPlanTier(override val code: String) : TrackingLog.Enum {
    BUSINESS(
        code = "business",
    ),
    LEGACY(
        code = "legacy",
    ),
    STARTER_TEAM(
        code = "starter_team",
    ),
    TEAM(
        code = "team",
    ),
}