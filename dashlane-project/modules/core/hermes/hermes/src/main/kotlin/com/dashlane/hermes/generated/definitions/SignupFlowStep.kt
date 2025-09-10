package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
enum class SignupFlowStep(override val code: String) : TrackingLog.Enum {
    CREATE_MASTER_PASSWORD(
        code = "create_master_password",
    ),
    ENTER_EMAIL_ADDRESS(
        code = "enter_email_address",
    ),
    INSTALL_EXTENSION(
        code = "install_extension",
    ),
    LOGIN_TO_ACCOUNT(
        code = "login_to_account",
    ),
    VERIFY_EMAIL(
        code = "verify_email",
    ),
}