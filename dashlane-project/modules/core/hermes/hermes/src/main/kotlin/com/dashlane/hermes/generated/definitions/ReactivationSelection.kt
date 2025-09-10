package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
enum class ReactivationSelection(override val code: String) : TrackingLog.Enum {
    CREATE_ACCOUNT(
        code = "create_account",
    ),
    LOGIN(
        code = "login",
    ),
    NEVER_ASK(
        code = "never_ask",
    ),
}