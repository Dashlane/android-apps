package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
enum class TwoFactorAuthenticationError(override val code: String) : TrackingLog.Enum {
    UNKNOWN_ERROR(
        code = "unknown_error",
    ),
    USER_OFFLINE_ERROR(
        code = "user_offline_error",
    ),
    WRONG_BACKUP_CODE_ERROR(
        code = "wrong_backup_code_error",
    ),
    WRONG_CODE_ERROR(
        code = "wrong_code_error",
    ),
    WRONG_PHONE_FORMAT_ERROR(
        code = "wrong_phone_format_error",
    ),
}