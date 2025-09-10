package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
enum class DataSource(override val code: String) : TrackingLog.Enum {
    EMPLOYEE_LOGINS(
        code = "employee_logins",
    ),
    NUDGES_FULL_ACTIVITY(
        code = "nudges_full_activity",
    ),
    PHISHING_ACTIVITY(
        code = "phishing_activity",
    ),
    URLS_FOR_ALLOWLIST(
        code = "urls_for_allowlist",
    ),
}