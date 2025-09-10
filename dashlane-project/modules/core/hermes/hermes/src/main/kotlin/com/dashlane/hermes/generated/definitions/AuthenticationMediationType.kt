package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
enum class AuthenticationMediationType(override val code: String) : TrackingLog.Enum {
    CONDITIONAL(
        code = "conditional",
    ),
    OPTIONAL(
        code = "optional",
    ),
    REQUIRED(
        code = "required",
    ),
    SILENT(
        code = "silent",
    ),
}