package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
enum class AuthMethodMigrationFlowStep(override val code: String) : TrackingLog.Enum {
    ALERT_MP_COMPROMISED(
        code = "alert_mp_compromised",
    ),
    ALERT_SSO_ENABLED(
        code = "alert_sso_enabled",
    ),
    ERROR(
        code = "error",
    ),
    PROCEED_TO_CHANGE_TO_MPLESS(
        code = "proceed_to_change_to_mpless",
    ),
    SKIP_OPTION_FOR_COMPROMISED_MP(
        code = "skip_option_for_compromised_mp",
    ),
    START_CHANGE_OF_MP(
        code = "start_change_of_mp",
    ),
    START_CHANGE_TO_MPLESS(
        code = "start_change_to_mpless",
    ),
    START_MIGRATION_TO_MPLESS(
        code = "start_migration_to_mpless",
    ),
    START_MIGRATION_TO_SSO(
        code = "start_migration_to_sso",
    ),
    SUCCESSFUL_MIGRATION_TO_MPLESS(
        code = "successful_migration_to_mpless",
    ),
    SUCCESSFUL_MIGRATION_TO_SSO(
        code = "successful_migration_to_sso",
    ),
    VIEW_OPTIONS_FOR_COMPROMISED_MP(
        code = "view_options_for_compromised_mp",
    ),
}