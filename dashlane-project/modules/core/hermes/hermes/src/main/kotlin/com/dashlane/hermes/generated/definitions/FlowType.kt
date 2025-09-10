package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
enum class FlowType(override val code: String) : TrackingLog.Enum {
    ACTIVATION(
        code = "activation",
    ),
    DEACTIVATION(
        code = "deactivation",
    ),
}