package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
enum class OtpAdditionError(override val code: String) : TrackingLog.Enum {
    MISSING_LOGIN(
        code = "missing_login",
    ),
    MULTIPLE_CREDENTIALS(
        code = "multiple_credentials",
    ),
    NO_SECRET(
        code = "no_secret",
    ),
    NON_OTP_QR_CODE(
        code = "non_otp_qr_code",
    ),
    NON_OTP_TEXT_CODE(
        code = "non_otp_text_code",
    ),
    UNKNOWN_ERROR(
        code = "unknown_error",
    ),
}