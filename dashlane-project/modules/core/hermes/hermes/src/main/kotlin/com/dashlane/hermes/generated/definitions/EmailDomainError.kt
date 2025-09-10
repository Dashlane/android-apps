package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
enum class EmailDomainError(override val code: String) : TrackingLog.Enum {
    INCORRECT_TEXT(
        code = "incorrect_text",
    ),
    NOT_FOUND(
        code = "not_found",
    ),
    OTHER_ERROR(
        code = "other_error",
    ),
}