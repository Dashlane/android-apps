package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
enum class AuthenticatorUserVerificationSource(override val code: String) : TrackingLog.Enum {
    AUTOFILL(
        code = "autofill",
    ),
    COPY_VAULT_ITEM(
        code = "copy_vault_item",
    ),
    PASSKEY_LOGIN(
        code = "passkey_login",
    ),
    PASSKEY_REGISTRATION(
        code = "passkey_registration",
    ),
}