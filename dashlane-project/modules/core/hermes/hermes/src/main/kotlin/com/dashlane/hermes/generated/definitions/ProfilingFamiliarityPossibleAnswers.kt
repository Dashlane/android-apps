package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
enum class ProfilingFamiliarityPossibleAnswers(override val code: String) : TrackingLog.Enum {
    NOT_FAMILIAR_AT_ALL(
        code = "not_familiar_at_all",
    ),
    NOT_VERY_FAMILIAR(
        code = "not_very_familiar",
    ),
    SOMEWHAT_FAMILIAR(
        code = "somewhat_familiar",
    ),
    SUPER_FAMILIAR(
        code = "super_familiar",
    ),
    VERY_FAMILIAR(
        code = "very_familiar",
    ),
}