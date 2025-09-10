package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
enum class BusinessDomain(override val code: String) : TrackingLog.Enum {
    PLATFORM(
        code = "platform",
    ),
    SYNC(
        code = "sync",
    ),
}