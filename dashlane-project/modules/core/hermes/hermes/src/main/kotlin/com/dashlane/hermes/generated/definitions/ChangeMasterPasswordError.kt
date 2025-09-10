package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
enum class ChangeMasterPasswordError(override val code: String) : TrackingLog.Enum {
    CIPHER_ERROR(
        code = "cipher_error",
    ),
    CONFIRMATION_ERROR(
        code = "confirmation_error",
    ),
    DECIPHER_ERROR(
        code = "decipher_error",
    ),
    DOWNLOAD_ERROR(
        code = "download_error",
    ),
    LOGIN_ERROR(
        code = "login_error",
    ),
    PASSWORDS_DONT_MATCH(
        code = "passwords_dont_match",
    ),
    SAME_PASSWORD_ERROR(
        code = "same_password_error",
    ),
    SYNC_FAILED_ERROR(
        code = "sync_failed_error",
    ),
    UNKNOWN_ERROR(
        code = "unknown_error",
    ),
    UPLOAD_ERROR(
        code = "upload_error",
    ),
    WEAK_PASSWORD_ERROR(
        code = "weak_password_error",
    ),
    WRONG_PASSWORD_ERROR(
        code = "wrong_password_error",
    ),
}