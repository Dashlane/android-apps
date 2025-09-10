package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
enum class TransferMethod(override val code: String) : TrackingLog.Enum {
    ACCOUNT_RECOVERY_KEY(
        code = "account_recovery_key",
    ),
    NOT_SELECTED(
        code = "not_selected",
    ),
    QR_CODE(
        code = "qr_code",
    ),
    SECURITY_CHALLENGE(
        code = "security_challenge",
    ),
}