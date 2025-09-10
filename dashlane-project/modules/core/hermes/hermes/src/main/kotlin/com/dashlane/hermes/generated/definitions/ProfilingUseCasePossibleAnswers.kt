package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
enum class ProfilingUseCasePossibleAnswers(override val code: String) : TrackingLog.Enum {
    CONTROL_REVOKED_MEMBERS_PASSWORD_ACCESS(
        code = "control_revoked_members_password_access",
    ),
    MEMBERS_USE_STRONG_PASSWORDS(
        code = "members_use_strong_passwords",
    ),
    NONE(
        code = "none",
    ),
    SECURELY_SHARE_PASSWORDS(
        code = "securely_share_passwords",
    ),
    STORE_PASSWORDS_IN_ONE_PLACE(
        code = "store_passwords_in_one_place",
    ),
}