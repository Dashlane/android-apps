package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
enum class AutofillMessageType(override val code: String) : TrackingLog.Enum {
    APP_META_INCORRECT_SIGNATURE(
        code = "app_meta_incorrect_signature",
    ),
    DUPLICATE_RISK(
        code = "duplicate_risk",
    ),
    FREE_ACCOUNT_PASSWORD_LIMIT_REACHED(
        code = "free_account_password_limit_reached",
    ),
    HTTP_UNSECURE_WEBSITE(
        code = "http_unsecure_website",
    ),
    KNOWN_INCORRECT_SIGNATURE(
        code = "known_incorrect_signature",
    ),
    KNOWN_SOURCE_ACCOUNT_FILL_MISMATCH(
        code = "known_source_account_fill_mismatch",
    ),
    LOGIN_ACCOUNT_PASSWORD_GENERATION(
        code = "login_account_password_generation",
    ),
    ONE_SIGNATURE_UNKNOWN(
        code = "one_signature_unknown",
    ),
    POTENTIAL_PHISHING_RISK(
        code = "potential_phishing_risk",
    ),
    UNKNOWN_SOURCE_ACCOUNT_FILL_MISMATCH(
        code = "unknown_source_account_fill_mismatch",
    ),
    UNSECURE_IFRAME(
        code = "unsecure_iframe",
    ),
    UNSECURE_IFRAME_SANDBOX(
        code = "unsecure_iframe_sandbox",
    ),
}