package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
enum class ChangeLoginEmailFlowStep(override val code: String) : TrackingLog.Enum {
    CANCEL_EMAIL_CHANGE(
        code = "cancel_email_change",
    ),
    ERROR(
        code = "error",
    ),
    RESEND_VERIFICATION_CODE(
        code = "resend_verification_code",
    ),
    START_EMAIL_CHANGE(
        code = "start_email_change",
    ),
    START_EMAIL_CHANGE_BULK(
        code = "start_email_change_bulk",
    ),
    SUBMIT_EMAIL_CHANGE(
        code = "submit_email_change",
    ),
    SUCCESSFUL_EMAIL_CHANGE(
        code = "successful_email_change",
    ),
    VERIFY_TOKEN(
        code = "verify_token",
    ),
}