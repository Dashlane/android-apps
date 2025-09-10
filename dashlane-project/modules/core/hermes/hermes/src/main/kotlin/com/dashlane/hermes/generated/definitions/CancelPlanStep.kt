package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
enum class CancelPlanStep(override val code: String) : TrackingLog.Enum {
    ABANDON(
        code = "abandon",
    ),
    CANCEL_ERROR(
        code = "cancel_error",
    ),
    CONTACT_SUPPORT(
        code = "contact_support",
    ),
    SELECTED_CANCELLATION_REASON(
        code = "selected_cancellation_reason",
    ),
    START(
        code = "start",
    ),
    SUCCESS_CANCEL(
        code = "success_cancel",
    ),
    SUCCESS_CANCEL_AND_ERROR_REFUND(
        code = "success_cancel_and_error_refund",
    ),
    SUCCESS_REFUND(
        code = "success_refund",
    ),
}