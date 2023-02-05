package com.dashlane.preference

class ConstantsPrefs {
    companion object {
        internal const val SKIP_INTRO = "skipIntro"
        internal const val LOGGED_USER = "lastLoggedUser"
        internal const val USER_LIST_HISTORY = "userListHistory"
        internal const val USER_LIST_HISTORY_MAX_SIZE = 5
        internal const val DEVICE_COUNTRY = "deviceCountry"
        internal const val DEVICE_COUNTRY_REFRESH = "deviceCountryRefreshTimestamp"
        internal const val DEVICE_EUROPEAN_UNION_STATUS = "deviceIsInEuropeanUnion"
        internal const val IS_USER_LOCKED = "isUserLocked"
        internal const val REFFERAL_ID = "referralUrl"
        internal const val ACCESS_KEY = "pref_api_auth_access_key"
        internal const val RSA_PUBLIC_KEY = "pref_rsa_public_key"
        internal const val PINCODE_ON = "pincodeOn"
        internal const val PINCODE_TRY_COUNT = "pincodeTryCount"
        internal const val USER_SETTINGS_BACKUP_TIME = "userSettingsBackupTime"
        internal const val MULTIPLE_ACCOUNT_LOADED_ON_THIS_DEVICE = "moreThanOneUserOnThisDevice"
        internal const val REGISTRATION_ID = "gid"
        internal const val SETTINGS_2FA_DISABLED = "disable2FA"
        internal const val SETTINGS_ON_LOGIN_PAYWALL = "userOnLoginPaywall"
        internal const val ACCOUNT_CREATION_DATE = "accountCreationDate"
        internal const val ALLOW_SEND_LOGS = "allowSendLogs"
        internal const val REGISTERED_AUTHENTICATOR_PUSH_ID = "registeredAuthenticatorPushId"

        

        internal const val CREDENTIALS_TIMESTAMP = "credentials_timestamp"

        const val UKI = "uki"
        const val PUBLIC_USER_ID = "public_user_id"
        const val PINCODE_VALUE = "pincodeValue"
        const val ANONYMOUS_USER = "anonID"
        const val INITIAL_RUN_FINISHED = "initialRunFinished"
        const val UITEST_FORCE_SCREENSHOT = "uitestForceScreenshot"
        const val TIMESTAMP_LABEL = "timestamp"
        const val MIGRATION_15 = "MigrationTo15"
        const val RUNS = "num"

        const val PREMIUM_SERVER_RESPONSE = "premiumServerResponse"
        const val SYNC_ONLY_ON_WIFI = "syncOnlyOnWifi"
        const val TIME_OUT_LOCK = "lockTimeOut"
        const val LOCK_ON_EXIT = "lockOnExit"
        const val UNLOCK_ITEMS_WITH_PIN_OR_FP = "unlockItemsWithPincode"

        const val USE_GOOGLE_FINGERPRINT = "useGoogleFingerprint"
        const val INVALIDATED_BIOMETRIC = "invalidatedBiometric"
        const val INSTALL_EVENT = "install_event"
        const val USER_ORIGIN = "userOrigin"
        const val WINDOW_SECURITY_FLAG_DISABLED = "windowSecurityDisabled"
        const val GRACE_PERIOD_END_NOTIFICATION_DONE = "gracePeriodEndNotified"
        const val TIMESTAMP_NEXT_PREMIUM_REMINDER = "nextPremiumReminderTimestamp"
        const val SHOW_PREMIUM_REMINDER = "showPremiumReminder"
        const val USER_NUMBER_DEVICES = "numberOfDevices"
        const val LAST_BACKUPSYNC_TIMESTAMP = "lastBackupTimestamp"
        const val OTP2SECURITY = "otp2"
        const val SECURITY_SETTINGS = "loginSecuritySettings"
        const val CLEAR_CLIPBOARD_ON_TIMEOUT = "clearclipboard"
        const val RUNNING_VERSION = "runningversion"
        const val FIRST_RUN_VERSION_CODE = "firstRunVersionCode"
        const val TOKEN_RETRIEVED_ON_PUSH = "tokenRetrievedOnPush"
        const val PASSWORD_GENERATOR_LENGTH = "pwdGeneratorLength"
        const val PASSWORD_GENERATOR_DIGITS = "pwdGeneratorDigits"
        const val PASSWORD_GENERATOR_LETTERS = "pwdGeneratorLetters"
        const val PASSWORD_GENERATOR_SYMBOLS = "pwdGeneratorSymbols"
        const val PASSWORD_GENERATOR_AMBIGUOUS = "pwdGeneratorAmbiguousChar"
        const val HOME_PAGE_GETTING_STARTED_PIN_IGNORE = "home_page_getting_started_pin_ignore"
        const val PREMIUM_RENEWAL_FIRST_NOTIFICATION_DONE = "premium_renewal_first_notification_done"
        const val PREMIUM_RENEWAL_SECOND_NOTIFICATION_DONE = "premium_renewal_second_notification_done"
        const val PREMIUM_RENEWAL_THIRD_NOTIFICATION_DONE = "premium_renewal_third_notification_done"
        const val AUTHENTIFIANTS_PREFERED_ORDER_CATEGORY = "authentifiants_prefered_order_category"
        const val SECURE_NOTES_PREFERED_ORDER_CATEGORY = "secure_notes_prefered_order_category"
        const val FULL_REFERRER = "full_referrer"
        const val REFERRED_BY = "referred_by"
        const val REFERRER_ORIGIN_PACKAGE = "referrer_origin_package"
        const val REFERRER_UNIQUE_REF_ID = "referrer_unique_ref_id"
        const val INSTALLATION_TIMESTAMP = "installation_timestamp"
        const val HAS_DESKTOP_DEVICE = "has_desktop_device"
        const val IN_APP_REVIEW_NEXT_SCHEDULE_TIMESTAMP = "nextRateTime"
        const val IN_APP_REVIEW_PREVIOUS_VERSION_CODE = "inAppReviewPreviousVersion"
        const val CALL_PERMISSION = "call_permission"

        const val NEED_POPUP_SPACE_REVOKED_FOR = "space_just_revoked"

        const val LAST_UL108_SENT = "lastUL108Sent"

        const val HAS_ACTIVATED_AUTOFILL_ONCE = "has_activated_autofill_once"

        const val IS_DARK_THEME_ENABLED = "isDarkThemeEnabled"

        

        const val PREMIUM_NEW_DEVICE_LATEST_TIMESTAMP = "premium_new_device_latest_timestamp"
        const val PREMIUM_NEW_DEVICE_DISPLAY_COUNT = "premium_new_device_display_count"

        

        const val LOCK_POPUP_LATEST_TIMESTAMP = "lock_popup_latest_timestamp"

        const val UKI_TEMPORARY_MONOBUCKET = "uki_temporary_monobucket"
        const val SETTINGS_SHOULD_SYNC = "settings_should_sync"

        const val LEGACY_KEY_SETTINGS = "settings"
        const val LEGACY_KEY_RSA_PRIVATE_KEY = "pref_rsa_private_key"
        const val LEGACY_KEY_FEATURE_FLIPPING = "pref_feature_flipping"

        const val HAS_FINISHED_M2D = "has_finished_m2d"
        const val HAS_STARTED_CHROME_IMPORT = "has_started_chrome_import"

        

        const val AUTOFILL_NOTIFICATION_DISMISS_COUNT = "autofill_notification_dismiss"

        

        const val PAUSED_APP_SOURCES_LIST = "paused_app_sources_list"
        const val PAUSED_WEB_SOURCES_LIST = "paused_web_sources_list"

        

        const val AUTOFILL_UPGRADE_POPUP_LATEST_TIMESTAMP = "autofill_upgrade_popup_latest_timestamp"

        

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

        

        const val AUDIT_DUPLICATES_PROCESSED = "audit_duplicates_processed"

        

        const val INSTALLATION_TRACKING_ID = "installationTrackingId"

        const val FOLLOW_UP_NOTIFICATION_SETTING = "follow_up_notification_setting"

        const val FOLLOW_UP_NOTIFICATION_DISCOVERY_SCREEN = "follow_up_notification_discovery_screen"
        const val FOLLOW_UP_NOTIFICATION_REMINDER_SCREEN = "follow_up_notification_reminder_screen"

        

        const val FOLLOW_UP_NOTIFICATION_LAST_ITEM_ID = "follow_up_notification_last_item_id"
        const val FOLLOW_UP_NOTIFICATION_LAST_NOTIFICATION_ID =
            "follow_up_notification_last_notification_id"
        const val FOLLOW_UP_NOTIFICATION_HAS_INTERACTED = "follow_up_notification_has_inteacted"

        

        const val MP_RESET_RECOVERY_STARTED = "mp_reset_recovery_started"

        

        const val VPN_THIRD_PARTY_INFOBOX_DISMISSED = "vpn_third_party_infobox_dismissed"

        

        const val VPN_THIRD_PARTY_GET_STARTED_DISPLAYED = "vpn_third_party_get_started_displayed"

        const val CRYPTO_MIGRATION_ATTEMPT_TIMESTAMP = "crypto_migration_attempt_timstamp"

        

        const val VAULT_REPORT_LATEST_TRIGGER_TIMESTAMP = "vault_report_latest_trigger_timestamp"

        

        const val AUTHENTICATOR_GET_STARTED_DISPLAYED = "authenticator_get_started_displayed"

        

        const val BIOMETRIC_SEAL_PADDING_MIGRATION_ATTEMPT = "biometric_seal_padding_migration"

        

        const val IS_FIRST_PASSWORD_MANAGER_LAUNCH_LOG_SENT = "is_first_password_manager_launch_log_sent"

        

        const val HAS_AUTOMATIC_2FA_TOKEN_COPY = "has_automatic_2fa_token_copy"
    }
}
