package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
enum class PriorityPage(override val code: String) : TrackingLog.Enum {
    ACCOUNT_CREATION_CREATE_ACCOUNT(
        code = "account_creation/create_account",
    ),
    ACCOUNT_CREATION_CREATE_MASTER_PASSWORD(
        code = "account_creation/create_master_password",
    ),
    ACCOUNT_CREATION_EMAIL(
        code = "account_creation/email",
    ),
    ACCOUNT_CREATION_TERMS_SERVICES(
        code = "account_creation/terms_services",
    ),
    ONBOARDING(
        code = "onboarding",
    ),
    ONBOARDING_TRUST_SCREENS(
        code = "onboarding/trust_screens",
    ),
    LOGIN_EMAIL(
        code = "login/email",
    ),
    LOGIN_TOKEN(
        code = "login/token",
    ),
    LOGIN_TOKEN_AUTHENTICATOR(
        code = "login/token/authenticator",
    ),
    LOGIN_TOKEN_EMAIL(
        code = "login/token/email",
    ),
    TOOLS_NEW_DEVICE(
        code = "tools/new_device",
    ),
    UNLOCK_BIOMETRIC(
        code = "unlock/biometric",
    ),
    UNLOCK_MP(
        code = "unlock/mp",
    ),
    UNLOCK_PIN(
        code = "unlock/pin",
    ),
}