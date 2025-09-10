package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
enum class SplunkSetupStep(override val code: String) : TrackingLog.Enum {
    ACTIVATE_SPLUNK_INTEGRATION(
        code = "activate_splunk_integration",
    ),
    CLICK_SET_UP(
        code = "click_set_up",
    ),
    DEACTIVATE_SPLUNK_INTEGRATION(
        code = "deactivate_splunk_integration",
    ),
    GENERIC_ERROR(
        code = "generic_error",
    ),
    INVALID_URL_ERROR(
        code = "invalid_url_error",
    ),
    MISMATCH_ADMIN_PROVISIONING_KEY_ERROR(
        code = "mismatch_admin_provisioning_key_error",
    ),
    SAVE_TOKENS(
        code = "save_tokens",
    ),
    TEAM_NOT_FOUND_ERROR(
        code = "team_not_found_error",
    ),
}