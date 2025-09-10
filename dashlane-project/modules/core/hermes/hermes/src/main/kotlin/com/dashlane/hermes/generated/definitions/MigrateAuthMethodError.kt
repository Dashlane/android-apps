package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
enum class MigrateAuthMethodError(override val code: String) : TrackingLog.Enum {
    SSO_FEATURE_BLOCKED(
        code = "sso_feature_blocked",
    ),
    SSO_LOGIN_CORRUPT(
        code = "sso_login_corrupt",
    ),
    SSO_SETUP_ERROR(
        code = "sso_setup_error",
    ),
    UNKNOWN_ERROR(
        code = "unknown_error",
    ),
}