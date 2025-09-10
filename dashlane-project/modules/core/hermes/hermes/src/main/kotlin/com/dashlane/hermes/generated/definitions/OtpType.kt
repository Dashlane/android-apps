package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
enum class OtpType(override val code: String) : TrackingLog.Enum {
    HOTP(
        code = "hotp",
    ),
    TOTP(
        code = "totp",
    ),
}