package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
enum class AuthenticatorNames(override val code: String) : TrackingLog.Enum {
    AUTHY(
        code = "authy",
    ),
    DUO(
        code = "duo",
    ),
    GOOGLE(
        code = "google",
    ),
    LASTPASS(
        code = "lastpass",
    ),
    MICROSOFT(
        code = "microsoft",
    ),
}