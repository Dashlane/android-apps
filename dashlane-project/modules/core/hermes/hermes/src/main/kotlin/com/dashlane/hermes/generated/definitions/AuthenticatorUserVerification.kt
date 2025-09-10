package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
enum class AuthenticatorUserVerification(override val code: String) : TrackingLog.Enum {
    DISCOURAGED(
        code = "discouraged",
    ),
    PREFERRED(
        code = "preferred",
    ),
    REQUIRED(
        code = "required",
    ),
}