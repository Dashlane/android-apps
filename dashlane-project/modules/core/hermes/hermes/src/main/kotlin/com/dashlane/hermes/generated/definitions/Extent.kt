package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
enum class Extent(override val code: String) : TrackingLog.Enum {
    FULL(
        code = "full",
    ),
    INITIAL(
        code = "initial",
    ),
    LIGHT(
        code = "light",
    ),
}