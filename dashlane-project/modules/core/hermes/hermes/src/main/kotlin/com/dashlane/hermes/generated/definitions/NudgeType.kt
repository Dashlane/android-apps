package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
enum class NudgeType(override val code: String) : TrackingLog.Enum {
    ADOPTION(
        code = "adoption",
    ),
    COMPROMISED_PASSWORDS(
        code = "compromised_passwords",
    ),
    PASSKEY(
        code = "passkey",
    ),
    PHISHING(
        code = "phishing",
    ),
    REUSED_PASSWORDS(
        code = "reused_passwords",
    ),
    WEAK_PASSWORDS(
        code = "weak_passwords",
    ),
}