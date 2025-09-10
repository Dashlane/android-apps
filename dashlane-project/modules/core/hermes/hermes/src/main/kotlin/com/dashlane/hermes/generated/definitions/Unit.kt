package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
enum class Unit(override val code: String) : TrackingLog.Enum {
    GB(
        code = "gb",
    ),
    MB(
        code = "mb",
    ),
    MILLISECONDS(
        code = "milliseconds",
    ),
    PERCENT(
        code = "percent",
    ),
    SECONDS(
        code = "seconds",
    ),
}