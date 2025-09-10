package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
enum class AccountCreationStatus(override val code: String) : TrackingLog.Enum {
    ERROR_ACCOUNT_ALREADY_EXISTS(
        code = "error_account_already_exists",
    ),
    ERROR_NOT_VALID_EMAIL(
        code = "error_not_valid_email",
    ),
    SUCCESS(
        code = "success",
    ),
}