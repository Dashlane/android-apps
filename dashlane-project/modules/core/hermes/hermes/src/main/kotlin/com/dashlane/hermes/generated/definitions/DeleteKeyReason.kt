package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
enum class DeleteKeyReason(override val code: String) : TrackingLog.Enum {
    NEW_RECOVERY_KEY_GENERATED(
        code = "new_recovery_key_generated",
    ),
    RECOVERY_KEY_USED(
        code = "recovery_key_used",
    ),
    SETTING_DISABLED(
        code = "setting_disabled",
    ),
    VAULT_KEY_CHANGED(
        code = "vault_key_changed",
    ),
}