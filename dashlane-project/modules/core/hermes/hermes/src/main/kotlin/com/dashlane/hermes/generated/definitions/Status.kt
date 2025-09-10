package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
enum class Status(override val code: String) : TrackingLog.Enum {
    ERROR_INVALID_SSO(
        code = "error_invalid_sso",
    ),
    ERROR_UNKNOWN(
        code = "error_unknown",
    ),
    ERROR_WRONG_BACKUP_CODE(
        code = "error_wrong_backup_code",
    ),
    ERROR_WRONG_BIOMETRIC(
        code = "error_wrong_biometric",
    ),
    ERROR_WRONG_EMAIL(
        code = "error_wrong_email",
    ),
    ERROR_WRONG_OTP(
        code = "error_wrong_otp",
    ),
    ERROR_WRONG_PASSWORD(
        code = "error_wrong_password",
    ),
    ERROR_WRONG_PIN(
        code = "error_wrong_pin",
    ),
    SUCCESS(
        code = "success",
    ),
}