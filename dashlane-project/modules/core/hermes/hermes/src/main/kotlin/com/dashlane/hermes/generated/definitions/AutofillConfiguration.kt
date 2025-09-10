package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
enum class AutofillConfiguration(override val code: String) : TrackingLog.Enum {
    AUTOFILL_DISABLED(
        code = "autofill_disabled",
    ),
    AUTOFILL_ENABLED(
        code = "autofill_enabled",
    ),
    AUTOLOGIN_DISABLED(
        code = "autologin_disabled",
    ),
    AUTOLOGIN_ENABLED(
        code = "autologin_enabled",
    ),
    LOGIN_AND_PASSWORDS_ONLY(
        code = "login_and_passwords_only",
    ),
    ONE_CLICK_FORM_FILL_DISABLED(
        code = "one_click_form_fill_disabled",
    ),
    ONE_CLICK_FORM_FILL_ENABLED(
        code = "one_click_form_fill_enabled",
    ),
    PHISHING_ALERTS_DISABLED(
        code = "phishing_alerts_disabled",
    ),
    PHISHING_ALERTS_ENABLED(
        code = "phishing_alerts_enabled",
    ),
}