package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
enum class BackupFileType(override val code: String) : TrackingLog.Enum {
    APPLE_PASSWORDS(
        code = "apple_passwords",
    ),
    CREDENTIAL_EXCHANGE_PROTOCOL(
        code = "credential_exchange_protocol",
    ),
    CSV(
        code = "csv",
    ),
    DASH(
        code = "dash",
    ),
    MANUAL_INPUT(
        code = "manual_input",
    ),
    PENDING_SELECTION(
        code = "pending_selection",
    ),
    SECURE_VAULT(
        code = "secure_vault",
    ),
    TXT(
        code = "txt",
    ),
    UNKNOWN(
        code = "unknown",
    ),
}