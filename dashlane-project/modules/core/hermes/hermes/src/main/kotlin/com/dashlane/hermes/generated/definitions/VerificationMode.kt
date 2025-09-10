package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
enum class VerificationMode(override val code: String) : TrackingLog.Enum {
    AUTHENTICATOR_APP(
        code = "authenticator_app",
    ),
    EMAIL_TOKEN(
        code = "email_token",
    ),
    NONE(
        code = "none",
    ),
    OTP1(
        code = "otp1",
    ),
    OTP2(
        code = "otp2",
    ),
}