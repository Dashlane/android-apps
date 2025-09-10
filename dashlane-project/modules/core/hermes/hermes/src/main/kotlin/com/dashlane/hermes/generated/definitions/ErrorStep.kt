package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
enum class ErrorStep(override val code: String) : TrackingLog.Enum {
    CHRONOLOGICAL(
        code = "chronological",
    ),
    DEDUPLICATE(
        code = "deduplicate",
    ),
    SHARING(
        code = "sharing",
    ),
    TREAT_PROBLEM(
        code = "treat_problem",
    ),
}