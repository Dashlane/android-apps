package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
enum class AuthenticatorAttachment(override val code: String) : TrackingLog.Enum {
    CROSS_PLATFORM(
        code = "cross_platform",
    ),
    PLATFORM(
        code = "platform",
    ),
}