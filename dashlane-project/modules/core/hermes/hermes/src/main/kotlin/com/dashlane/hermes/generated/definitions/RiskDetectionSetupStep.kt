package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
enum class RiskDetectionSetupStep(override val code: String) : TrackingLog.Enum {
    ADMIN_SEE_CRD_LOG_FROM_AUDIT_LOGS(
        code = "admin_see_crd_log_from_audit_logs",
    ),
    ADMIN_SEE_CRD_LOG_FROM_INSIGHTS(
        code = "admin_see_crd_log_from_insights",
    ),
    CANCEL_ROTATE_TOKEN(
        code = "cancel_rotate_token",
    ),
    CLICK_CHROME_ADMX_LINK(
        code = "click_chrome_admx_link",
    ),
    CONFIRM_ROTATE_TOKEN(
        code = "confirm_rotate_token",
    ),
    COPY_CONFIGURATION_TOKEN(
        code = "copy_configuration_token",
    ),
    COPY_GPO_CONFIGURATION_TOKEN(
        code = "copy_gpo_configuration_token",
    ),
    COPY_INTUNE_CHROME_DEVICE_VALUE(
        code = "copy_intune_chrome_device_value",
    ),
    COPY_INTUNE_CONFIGURATION_TOKEN(
        code = "copy_intune_configuration_token",
    ),
    COPY_INTUNE_EDGE_DEVICE_VALUE(
        code = "copy_intune_edge_device_value",
    ),
    COPY_JAMF_FORCE_INSTALL_CHROME_PREFERENCE_DOMAIN(
        code = "copy_jamf_force_install_chrome_preference_domain",
    ),
    COPY_JAMF_FORCE_INSTALL_CHROME_PROPERTY_LIST(
        code = "copy_jamf_force_install_chrome_property_list",
    ),
    COPY_JAMF_FORCE_INSTALL_EDGE_PREFERENCE_DOMAIN(
        code = "copy_jamf_force_install_edge_preference_domain",
    ),
    COPY_JAMF_FORCE_INSTALL_EDGE_PROPERTY_LIST(
        code = "copy_jamf_force_install_edge_property_list",
    ),
    COPY_JAMF_POLICIES_CHROME_PREFERENCE_DOMAIN(
        code = "copy_jamf_policies_chrome_preference_domain",
    ),
    COPY_JAMF_POLICIES_EDGE_PREFERENCE_DOMAIN(
        code = "copy_jamf_policies_edge_preference_domain",
    ),
    COPY_JAMF_POLICIES_PROPERTY_LIST(
        code = "copy_jamf_policies_property_list",
    ),
    DOWNLOAD_CHROME_AND_EDGE_GPO_FILE(
        code = "download_chrome_and_edge_gpo_file",
    ),
    DOWNLOAD_CHROME_GPO_FILE(
        code = "download_chrome_gpo_file",
    ),
    DOWNLOAD_EDGE_GPO_FILE(
        code = "download_edge_gpo_file",
    ),
    DOWNLOAD_INTUNE_SCRIPT_FILE(
        code = "download_intune_script_file",
    ),
    DOWNLOAD_RISK_ACTIVITY_CSV(
        code = "download_risk_activity_csv",
    ),
    ERROR_LOAD_RISK_DETECTION_INSIGHTS(
        code = "error_load_risk_detection_insights",
    ),
    EXPORT_RISK_DETECTION_CSV(
        code = "export_risk_detection_csv",
    ),
    FILTER_LOGS_LAST_30_DAYS(
        code = "filter_logs_last_30_days",
    ),
    FILTER_LOGS_LAST_7_DAYS(
        code = "filter_logs_last_7_days",
    ),
    ROTATE_TOKEN(
        code = "rotate_token",
    ),
    SEE_ACTIVITY_LOGS(
        code = "see_activity_logs",
    ),
    START_SETUP(
        code = "start_setup",
    ),
    TURN_OFF(
        code = "turn_off",
    ),
    TURN_ON(
        code = "turn_on",
    ),
}