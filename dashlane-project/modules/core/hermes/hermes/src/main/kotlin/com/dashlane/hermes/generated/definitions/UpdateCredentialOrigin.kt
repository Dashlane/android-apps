package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
enum class UpdateCredentialOrigin(override val code: String) : TrackingLog.Enum {
    CSV_IMPORT(
        code = "csv_import",
    ),
    MANUAL(
        code = "manual",
    ),
    PASSWORD_CHANGER(
        code = "password_changer",
    ),
    PASSWORD_HISTORY(
        code = "password_history",
    ),
    SECURE_VAULT_IMPORT(
        code = "secure_vault_import",
    ),
}