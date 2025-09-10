package com.dashlane.preference

class ConstantsPrefs {
    companion object {
        internal const val LOGGED_USER = "lastLoggedUser"
        internal const val USER_LIST_HISTORY = "userListHistory"
        internal const val USER_LIST_HISTORY_MAX_SIZE = 5

        @Deprecated("Production clean in progress since Jan 22 2025- to remove after good adoption")
        internal const val PHISHING_MODEL_LAST_CHECK_TIMESTAMP = "phishing_model_last_check_timestamp"

        @Deprecated("Production clean in progress since Jan 22 2025 - to remove after good adoption")
        internal const val PHISHING_WARNING_IGNORED_WEBSITES = "phishing_warning_ignored_websites"

        internal const val IS_USER_LOCKED = "isUserLocked"
        internal const val ACCESS_KEY = "pref_api_auth_access_key"
        internal const val RSA_PUBLIC_KEY = "pref_rsa_public_key"
        internal const val PINCODE_ON = "pincodeOn"
        internal const val PINCODE_TRY_COUNT = "pincodeTryCount"
        internal const val PINCODE_LENGTH = "pincodeLength"
        internal const val USER_SETTINGS_BACKUP_TIME = "userSettingsBackupTime"
        internal const val MULTIPLE_ACCOUNT_LOADED_ON_THIS_DEVICE = "moreThanOneUserOnThisDevice"
        internal const val REGISTRATION_ID = "gid"
        internal const val SETTINGS_2FA_DISABLED = "disable2FA"
        internal const val SETTINGS_ON_LOGIN_PAYWALL = "userOnLoginPaywall"
        internal const val ACCOUNT_CREATION_DATE = "accountCreationDate"
        internal const val ALLOW_SEND_LOGS = "allowSendLogs"
        internal const val USER_ACTIVITY_UPDATE_DATE = "userActivityUpdateDate"
        internal const val USER_SUBSCRIPTION_CODE = "userSubscriptionCode"
        internal const val MPLESS_ARK_ENABLED = "mplessARKEnabled"

        internal const val CREDENTIALS_TIMESTAMP = "credentials_timestamp"

        const val PUBLIC_USER_ID = "public_user_id"
        const val INITIAL_RUN_FINISHED = "initialRunFinished"
        const val UITEST_FORCE_SCREENSHOT = "uitestForceScreenshot"
        const val TIMESTAMP_LABEL = "timestamp"
        const val MIGRATION_15 = "MigrationTo15"
        const val RUNS = "num"

        const val TIME_OUT_LOCK = "lockTimeOut"
        const val LOCK_ON_EXIT = "lockOnExit"

        const val USE_GOOGLE_FINGERPRINT = "useGoogleFingerprint"
        const val INVALIDATED_BIOMETRIC = "invalidatedBiometric"
        const val DISABLED_ACCOUNT_RECOVERY_KEY = "disabledAccountRecoveryKey"
        const val USER_ORIGIN = "userOrigin"
        const val WINDOW_SECURITY_FLAG_DISABLED = "windowSecurityDisabled"
        const val GRACE_PERIOD_END_NOTIFICATION_DONE = "gracePeriodEndNotified"
        const val USER_NUMBER_DEVICES = "numberOfDevices"
        const val OTP2SECURITY = "otp2"
        const val ACCOUNT_TYPE = "pref_account_type"
        const val SECURITY_SETTINGS = "loginSecuritySettings"
        const val CLEAR_CLIPBOARD_ON_TIMEOUT = "clearclipboard"
        const val PASSWORD_GENERATOR_LENGTH = "pwdGeneratorLength"
        const val PASSWORD_GENERATOR_DIGITS = "pwdGeneratorDigits"
        const val PASSWORD_GENERATOR_LETTERS = "pwdGeneratorLetters"
        const val PASSWORD_GENERATOR_SYMBOLS = "pwdGeneratorSymbols"
        const val PASSWORD_GENERATOR_AMBIGUOUS = "pwdGeneratorAmbiguousChar"
        const val HOME_PAGE_GETTING_STARTED_PIN_IGNORE = "home_page_getting_started_pin_ignore"
        const val PREMIUM_RENEWAL_FIRST_NOTIFICATION_DONE = "premium_renewal_first_notification_done"
        const val PREMIUM_RENEWAL_SECOND_NOTIFICATION_DONE = "premium_renewal_second_notification_done"
        const val PREMIUM_RENEWAL_THIRD_NOTIFICATION_DONE = "premium_renewal_third_notification_done"
        const val REFERRED_BY = "referred_by"
        const val IN_APP_REVIEW_NEXT_SCHEDULE_TIMESTAMP = "nextRateTime"
        const val IN_APP_REVIEW_PREVIOUS_VERSION_CODE = "inAppReviewPreviousVersion"

        const val NEED_POPUP_SPACE_REVOKED_FOR = "space_just_revoked"

        const val HAS_ACTIVATED_AUTOFILL_ONCE = "has_activated_autofill_once"

        const val PREMIUM_NEW_DEVICE_LATEST_TIMESTAMP = "premium_new_device_latest_timestamp"
        const val PREMIUM_NEW_DEVICE_DISPLAY_COUNT = "premium_new_device_display_count"

        const val LOCK_POPUP_LATEST_TIMESTAMP = "lock_popup_latest_timestamp"

        const val UKI_TEMPORARY_MONOBUCKET = "uki_temporary_monobucket"
        const val SETTINGS_SHOULD_SYNC = "settings_should_sync"

        const val HAS_FINISHED_M2D = "has_finished_m2d"

        const val AUTOFILL_NOTIFICATION_DISMISS_COUNT = "autofill_notification_dismiss"

        const val AUTOFILL_NOTIFICATION_CHROME_DISMISS_COUNT = "autofill_notification_chrome_dismiss"

        const val PAUSED_APP_SOURCES_LIST = "paused_app_sources_list"
        const val PAUSED_WEB_SOURCES_LIST = "paused_web_sources_list"

        const val BACKUP_TOKEN_SET = "backup_token_set"

        const val LAST_SHOWN_AVAILABLE_UPDATE_DATE = "last_shown_available_update_date"

        const val AUTOFILL_REMEMBER_ACCOUNT_FOR_APP_SOURCES_LIST = "autofill_remember_app_sources_list"
        const val AUTOFILL_REMEMBER_ACCOUNT_FOR_WEB_SOURCES_LIST = "autofill_remember_web_sources_list"

        internal const val USE_INLINE_AUTOFILL_SETTING = "useInlineAutofillSetting"

        internal const val REQUEST_DISPLAY_KEYBOARD_ANNOUNCEMENT = "requestDisplayKeyboardAnnouncement"

        const val KEYBOARD_AUTOFILL_ANNOUNCEMENT_TIMESTAMP = "keyboardAutofillAnnouncementTimestamp"
        const val HAS_SEEN_KEYBOARD_ON_BOARDING_SUGGESTION = "hasSeenKeyboardOnBoardingSuggestion"

        internal const val LAST_OS_VERSION = "lastOSVersion"

        const val AUTOFILL_REMEMBER_SECURITY_WARNINGS_INCORRECT_JSON = "autofill_security_warnings_incorrect_json"
        const val AUTOFILL_REMEMBER_SECURITY_WARNINGS_UNKNOWN_JSON = "autofill_security_warnings_unknown_json"

        const val INSTALLATION_TRACKING_ID = "installationTrackingId"

        const val FOLLOW_UP_NOTIFICATION_SETTING = "follow_up_notification_setting"

        const val FOLLOW_UP_NOTIFICATION_DISCOVERY_SCREEN = "follow_up_notification_discovery_screen"
        const val FOLLOW_UP_NOTIFICATION_REMINDER_SCREEN = "follow_up_notification_reminder_screen"

        const val FOLLOW_UP_NOTIFICATION_LAST_ITEM_ID = "follow_up_notification_last_item_id"
        const val FOLLOW_UP_NOTIFICATION_LAST_NOTIFICATION_ID =
            "follow_up_notification_last_notification_id"
        const val FOLLOW_UP_NOTIFICATION_HAS_INTERACTED = "follow_up_notification_has_inteacted"

        const val MP_RESET_RECOVERY_STARTED = "mp_reset_recovery_started"

        const val CRYPTO_MIGRATION_ATTEMPT_TIMESTAMP = "crypto_migration_attempt_timstamp"

        const val VAULT_REPORT_LATEST_TRIGGER_TIMESTAMP = "vault_report_latest_trigger_timestamp"

        const val BIOMETRIC_SEAL_PADDING_MIGRATION_ATTEMPT = "biometric_seal_padding_migration"

        const val IS_FIRST_PASSWORD_MANAGER_LAUNCH_LOG_SENT = "is_first_password_manager_launch_log_sent"

        const val HAS_AUTOMATIC_2FA_TOKEN_COPY = "has_automatic_2fa_token_copy"

        const val IS_ANTI_PHISHING_ENABLED = "is_anti_phishing_enabled"

        const val CAN_SHOW_AUTOFILL_ANNOUNCEMENT = "can_show_autofill_announcement"

        const val CAN_SHOW_CHROME_ANNOUNCEMENT = "can_show_chrome_announcement"

        const val LABS_ACTIVATED = "labs_activated"

        const val TRIAL_ENDED_ANNOUNCEMENT_DISPLAY_TIMESTAMP = "trial_ended_announcement_display_timestamp"
    }
}