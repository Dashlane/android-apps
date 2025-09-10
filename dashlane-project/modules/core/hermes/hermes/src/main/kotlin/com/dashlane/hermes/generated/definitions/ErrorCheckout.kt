package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
enum class ErrorCheckout(override val code: String) : TrackingLog.Enum {
    AUTHENTICATION(
        code = "authentication",
    ),
    CARD_DECLINED(
        code = "card_declined",
    ),
    EXPIRED_CARD(
        code = "expired_card",
    ),
    UNEXPECTED_UNKNOWN(
        code = "unexpected_unknown",
    ),
}