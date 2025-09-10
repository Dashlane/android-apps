package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
enum class SsoSetupStep(override val code: String) : TrackingLog.Enum {
    ADD_CONFIGURATION_TO_SERVICE_HOST(
        code = "add_configuration_to_service_host",
    ),
    CHOOSE_YOUR_SSO_SOLUTION(
        code = "choose_your_sso_solution",
    ),
    CLEAR_SSO_SETTINGS(
        code = "clear_sso_settings",
    ),
    COMPLETE_SSO_SETUP(
        code = "complete_sso_setup",
    ),
    CREATE_SSO_APPLICATION(
        code = "create_sso_application",
    ),
    EMAIL_DOMAIN_ERROR(
        code = "email_domain_error",
    ),
    GENERATE_AND_SAVE_CONFIGURATION(
        code = "generate_and_save_configuration",
    ),
    RETURN_TO_SSO_SELECTION(
        code = "return_to_sso_selection",
    ),
    SELECT_ENCRYPTION_SERVICE_PLATFORM(
        code = "select_encryption_service_platform",
    ),
    SET_UP_ENCRYPTION_SERVICE_SETTINGS(
        code = "set_up_encryption_service_settings",
    ),
    SUBMIT_EMAIL_DOMAIN(
        code = "submit_email_domain",
    ),
    TEST_SSO_CONNECTION(
        code = "test_sso_connection",
    ),
    TURN_ON_SSO(
        code = "turn_on_sso",
    ),
    UPDATE_IDP_SETTINGS(
        code = "update_idp_settings",
    ),
    VALIDATE_IDP_METADATA(
        code = "validate_idp_metadata",
    ),
    VERIFY_DOMAIN(
        code = "verify_domain",
    ),
}