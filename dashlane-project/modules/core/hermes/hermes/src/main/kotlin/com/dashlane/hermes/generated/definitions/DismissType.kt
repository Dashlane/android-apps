package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
enum class DismissType(override val code: String) : TrackingLog.Enum {
    CANCEL(
        code = "cancel",
    ),
    CLOSE(
        code = "close",
    ),
    CLOSE_CROSS(
        code = "close_cross",
    ),
    CLOSE_ESCAPE(
        code = "close_escape",
    ),
    CLOSE_SECURITY(
        code = "close_security",
    ),
    DO_NOT_TRUST(
        code = "do_not_trust",
    ),
    NEVER(
        code = "never",
    ),
    TRUST(
        code = "trust",
    ),
    UNFOCUS(
        code = "unfocus",
    ),
    USE_LOCAL_PASSKEY(
        code = "use_local_passkey",
    ),
}