package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
enum class ReasonToExtendTrialAnswer(override val code: String) : TrackingLog.Enum {
    CHOSE_OTHER_PASSWORD_MANAGER(
        code = "chose_other_password_manager",
    ),
    FAMILIAR_WITH_DASHLANE(
        code = "familiar_with_dashlane",
    ),
    MISSING_CRITICAL_FEATURES(
        code = "missing_critical_features",
    ),
    NEED_HELP_SCIM(
        code = "need_help_scim",
    ),
    NEED_HELP_SSO(
        code = "need_help_sso",
    ),
    NEED_MORE_TIME_CONVINCE(
        code = "need_more_time_convince",
    ),
    NEED_MORE_TIME_TEAM_USAGE(
        code = "need_more_time_team_usage",
    ),
    NEED_SALES_REP(
        code = "need_sales_rep",
    ),
    NEVER_USED_BEFORE(
        code = "never_used_before",
    ),
    NO_INTERNAL_USAGE(
        code = "no_internal_usage",
    ),
    NO_NEED_FOR_PASSWORD_MANAGER(
        code = "no_need_for_password_manager",
    ),
    NOT_AWARE_TRIAL_ENDING(
        code = "not_aware_trial_ending",
    ),
    OTHER(
        code = "other",
    ),
    TECHNICAL_ISSUES(
        code = "technical_issues",
    ),
    TOO_EXPENSIVE(
        code = "too_expensive",
    ),
    USED_ANOTHER_PASSWORD_MANAGER(
        code = "used_another_password_manager",
    ),
}