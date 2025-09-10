package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
enum class Plan(override val code: String) : TrackingLog.Enum {
    BUSINESS(
        code = "business",
    ),
    BUSINESS_PLUS(
        code = "business_plus",
    ),
    ESSENTIALS(
        code = "essentials",
    ),
    FAMILY(
        code = "family",
    ),
    FREE(
        code = "free",
    ),
    PREMIUM(
        code = "premium",
    ),
    STANDARD(
        code = "standard",
    ),
    STARTER(
        code = "starter",
    ),
}