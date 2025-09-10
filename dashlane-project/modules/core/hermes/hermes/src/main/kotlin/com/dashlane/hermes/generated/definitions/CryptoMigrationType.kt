package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
enum class CryptoMigrationType(override val code: String) : TrackingLog.Enum {
    MASTER_PASSWORD_CHANGE(
        code = "master_password_change",
    ),
    MASTER_PASSWORD_TO_SSO(
        code = "master_password_to_sso",
    ),
    MIGRATE_LEGACY(
        code = "migrate_legacy",
    ),
    SETTINGS_APPLY_LOCALLY(
        code = "settings_apply_locally",
    ),
    SETTINGS_CHANGE(
        code = "settings_change",
    ),
    SSO_TO_MASTER_PASSWORD(
        code = "sso_to_master_password",
    ),
    TEAM_ENFORCED(
        code = "team_enforced",
    ),
}