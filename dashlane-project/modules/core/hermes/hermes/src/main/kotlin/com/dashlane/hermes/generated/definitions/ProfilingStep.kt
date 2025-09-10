package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
enum class ProfilingStep(override val code: String) : TrackingLog.Enum {
    FAMILIARITY_WITH_PASSWORD_MANAGERS(
        code = "familiarity_with_password_managers",
    ),
    FEATURES(
        code = "features",
    ),
    TEAM_SIZE(
        code = "team_size",
    ),
    USE_CASE(
        code = "use_case",
    ),
}