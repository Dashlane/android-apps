package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
enum class MatchType(override val code: String) : TrackingLog.Enum {
    ASSOCIATED_APP(
        code = "associated_app",
    ),
    ASSOCIATED_WEBSITE(
        code = "associated_website",
    ),
    CREATED_PASSWORD(
        code = "created_password",
    ),
    EXPLORE_PASSWORDS(
        code = "explore_passwords",
    ),
    REGULAR(
        code = "regular",
    ),
    REMEMBERED(
        code = "remembered",
    ),
    USER_ASSOCIATED_WEBSITE(
        code = "user_associated_website",
    ),
}