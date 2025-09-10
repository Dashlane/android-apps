package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
enum class PaymentMethod(override val code: String) : TrackingLog.Enum {
    CREDIT_CARD(
        code = "credit_card",
    ),
    INVOICE(
        code = "invoice",
    ),
}