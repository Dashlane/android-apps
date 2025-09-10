package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
enum class PlanBillingPeriod(override val code: String) : TrackingLog.Enum {
    MONTHLY(
        code = "monthly",
    ),
    YEARLY(
        code = "yearly",
    ),
}