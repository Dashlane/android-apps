package com.dashlane.preference

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import com.dashlane.preference.ConstantsPrefs.Companion.ACCESS_KEY
import com.dashlane.preference.ConstantsPrefs.Companion.ACCOUNT_CREATION_DATE
import com.dashlane.preference.ConstantsPrefs.Companion.ACCOUNT_TYPE
import com.dashlane.preference.ConstantsPrefs.Companion.AUTHENTICATOR_GET_STARTED_DISPLAYED
import com.dashlane.preference.ConstantsPrefs.Companion.BIOMETRIC_SEAL_PADDING_MIGRATION_ATTEMPT
import com.dashlane.preference.ConstantsPrefs.Companion.CREDENTIALS_TIMESTAMP
import com.dashlane.preference.ConstantsPrefs.Companion.CRYPTO_MIGRATION_ATTEMPT_TIMESTAMP
import com.dashlane.preference.ConstantsPrefs.Companion.FOLLOW_UP_NOTIFICATION_DISCOVERY_SCREEN
import com.dashlane.preference.ConstantsPrefs.Companion.FOLLOW_UP_NOTIFICATION_HAS_INTERACTED
import com.dashlane.preference.ConstantsPrefs.Companion.FOLLOW_UP_NOTIFICATION_LAST_ITEM_ID
import com.dashlane.preference.ConstantsPrefs.Companion.FOLLOW_UP_NOTIFICATION_LAST_NOTIFICATION_ID
import com.dashlane.preference.ConstantsPrefs.Companion.FOLLOW_UP_NOTIFICATION_REMINDER_SCREEN
import com.dashlane.preference.ConstantsPrefs.Companion.FOLLOW_UP_NOTIFICATION_SETTING
import com.dashlane.preference.ConstantsPrefs.Companion.HAS_AUTOMATIC_2FA_TOKEN_COPY
import com.dashlane.preference.ConstantsPrefs.Companion.HAS_FINISHED_M2D
import com.dashlane.preference.ConstantsPrefs.Companion.HAS_SEEN_KEYBOARD_ON_BOARDING_SUGGESTION
import com.dashlane.preference.ConstantsPrefs.Companion.IN_APP_REVIEW_NEXT_SCHEDULE_TIMESTAMP
import com.dashlane.preference.ConstantsPrefs.Companion.IN_APP_REVIEW_PREVIOUS_VERSION_CODE
import com.dashlane.preference.ConstantsPrefs.Companion.KEYBOARD_AUTOFILL_ANNOUNCEMENT_TIMESTAMP
import com.dashlane.preference.ConstantsPrefs.Companion.LAST_OS_VERSION
import com.dashlane.preference.ConstantsPrefs.Companion.LAST_SHOWN_AVAILABLE_UPDATE_DATE
import com.dashlane.preference.ConstantsPrefs.Companion.MP_RESET_RECOVERY_STARTED
import com.dashlane.preference.ConstantsPrefs.Companion.PINCODE_ON
import com.dashlane.preference.ConstantsPrefs.Companion.PINCODE_TRY_COUNT
import com.dashlane.preference.ConstantsPrefs.Companion.PUBLIC_USER_ID
import com.dashlane.preference.ConstantsPrefs.Companion.REFFERAL_ID
import com.dashlane.preference.ConstantsPrefs.Companion.REGISTERED_AUTHENTICATOR_PUSH_ID
import com.dashlane.preference.ConstantsPrefs.Companion.REQUEST_DISPLAY_KEYBOARD_ANNOUNCEMENT
import com.dashlane.preference.ConstantsPrefs.Companion.RSA_PUBLIC_KEY
import com.dashlane.preference.ConstantsPrefs.Companion.SETTINGS_2FA_DISABLED
import com.dashlane.preference.ConstantsPrefs.Companion.SETTINGS_ON_LOGIN_PAYWALL
import com.dashlane.preference.ConstantsPrefs.Companion.SETTINGS_SHOULD_SYNC
import com.dashlane.preference.ConstantsPrefs.Companion.UKI_TEMPORARY_MONOBUCKET
import com.dashlane.preference.ConstantsPrefs.Companion.USER_ACTIVITY_UPDATE_DATE
import com.dashlane.preference.ConstantsPrefs.Companion.USER_NUMBER_DEVICES
import com.dashlane.preference.ConstantsPrefs.Companion.USER_SETTINGS_BACKUP_TIME
import com.dashlane.preference.ConstantsPrefs.Companion.USE_INLINE_AUTOFILL_SETTING
import com.dashlane.preference.ConstantsPrefs.Companion.VAULT_REPORT_LATEST_TRIGGER_TIMESTAMP
import com.dashlane.preference.ConstantsPrefs.Companion.VPN_THIRD_PARTY_GET_STARTED_DISPLAYED
import com.dashlane.preference.ConstantsPrefs.Companion.VPN_THIRD_PARTY_INFOBOX_DISMISSED
import com.dashlane.session.SessionManager
import com.dashlane.session.Username
import com.dashlane.util.MD5Hash
import java.time.Instant

abstract class UserPreferencesManager(
    private val context: Context,
    val sessionManager: SessionManager? = null
) : DashlanePreferencesManager() {

    var accessKey: String? by stringPreference(ACCESS_KEY)
    var accountType: String? by stringPreference(ACCOUNT_TYPE)
    var publicKey: String? by stringPreference(RSA_PUBLIC_KEY)
    var referralId by stringPreference(REFFERAL_ID)
    var isPinCodeOn by booleanPreference(PINCODE_ON)
    var pinCodeTryCount by intPreference(PINCODE_TRY_COUNT)

    var ukiRequiresMonobucketConfirmation by booleanPreference(UKI_TEMPORARY_MONOBUCKET)
    var userSettingsShouldSync by booleanPreference(SETTINGS_SHOULD_SYNC)
    var userSettingsBackupTime: Instant
        get() = Instant.ofEpochMilli(userSettingsBackupTimeMillis)
        set(value) {
            userSettingsBackupTimeMillis = value.toEpochMilli()
        }
    var userSettingsBackupTimeMillis by longPreference(USER_SETTINGS_BACKUP_TIME)

    var is2FADisabled by booleanPreference(SETTINGS_2FA_DISABLED)

    var isOnLoginPaywall by booleanPreference(SETTINGS_ON_LOGIN_PAYWALL)

    var hasFinishedM2D by booleanPreference(HAS_FINISHED_M2D)

    var hasInlineAutofill by booleanPreference(USE_INLINE_AUTOFILL_SETTING, true)
    var requestDisplayKeyboardAnnouncement by booleanPreference(
        REQUEST_DISPLAY_KEYBOARD_ANNOUNCEMENT
    )

    var keyboardAutofillAnnouncementTimestamp by longPreference(
        KEYBOARD_AUTOFILL_ANNOUNCEMENT_TIMESTAMP
    )
    var hasSeenKeyboardOnBoardingSuggestion by booleanPreference(
        HAS_SEEN_KEYBOARD_ON_BOARDING_SUGGESTION
    )

    var publicUserId by stringPreference(PUBLIC_USER_ID)
    var devicesCount by intPreference(USER_NUMBER_DEVICES)

    var lastOsVersion by intPreference(LAST_OS_VERSION, Build.VERSION.SDK_INT)

    var hasAutomatic2faTokenCopy by booleanPreference(HAS_AUTOMATIC_2FA_TOKEN_COPY, true)

    
    var registeredAuthenticatorPushId by stringPreference(REGISTERED_AUTHENTICATOR_PUSH_ID)

    var lastShownAvailableUpdateDate: Instant
        get() = Instant.ofEpochSecond(getLong(LAST_SHOWN_AVAILABLE_UPDATE_DATE, 0))
        set(value) {
            putLong(LAST_SHOWN_AVAILABLE_UPDATE_DATE, value.epochSecond)
        }
    var accountCreationDate: Instant
        get() = Instant.ofEpochSecond(getLong(ACCOUNT_CREATION_DATE, 0))
        set(value) {
            putLong(ACCOUNT_CREATION_DATE, value.epochSecond)
        }
    var credentialsSaveDate: Instant
        get() = Instant.ofEpochMilli(getLong(CREDENTIALS_TIMESTAMP, 0))
        set(value) {
            putLong(CREDENTIALS_TIMESTAMP, value.toEpochMilli())
        }

    var inAppReviewNextDate: Instant?
        get() {
            val timestamp = getLong(IN_APP_REVIEW_NEXT_SCHEDULE_TIMESTAMP, -1L)
            if (timestamp == -1L) return null

            return Instant.ofEpochMilli(timestamp)
        }
        set(value) {
            putLong(IN_APP_REVIEW_NEXT_SCHEDULE_TIMESTAMP, value?.toEpochMilli() ?: -1L)
        }

    var inAppReviewPreviousVersionCode by stringPreference(IN_APP_REVIEW_PREVIOUS_VERSION_CODE, "")
    var isFollowUpNotificationChecked by booleanPreference(FOLLOW_UP_NOTIFICATION_SETTING, true)
    var hasSeenFollowUpNotificationDiscoveryScreen: Boolean by booleanPreference(
        FOLLOW_UP_NOTIFICATION_DISCOVERY_SCREEN,
        false
    )
    var hasAcknowledgedFollowUpNotificationReminderScreen: Boolean by booleanPreference(
        FOLLOW_UP_NOTIFICATION_REMINDER_SCREEN,
        false
    )
    var lastFollowUpNotificationItem: Triple<String, String, Boolean>
        get() = Triple(
            getString(FOLLOW_UP_NOTIFICATION_LAST_ITEM_ID) ?: "",
            getString(FOLLOW_UP_NOTIFICATION_LAST_NOTIFICATION_ID) ?: "",
            getBoolean(FOLLOW_UP_NOTIFICATION_HAS_INTERACTED, false)
        )
        set(value) {
            putString(FOLLOW_UP_NOTIFICATION_LAST_ITEM_ID, value.first)
            putString(FOLLOW_UP_NOTIFICATION_LAST_NOTIFICATION_ID, value.second)
            putBoolean(FOLLOW_UP_NOTIFICATION_HAS_INTERACTED, value.third)
        }

    var isMpResetRecoveryStarted by booleanPreference(MP_RESET_RECOVERY_STARTED, false)

    var isThirdPartyVpnInfoboxDismissed by booleanPreference(
        VPN_THIRD_PARTY_INFOBOX_DISMISSED,
        false
    )

    var isThirdPartyVpnGetStartedDisplayed by booleanPreference(
        VPN_THIRD_PARTY_GET_STARTED_DISPLAYED,
        false
    )

    var isAuthenticatorGetStartedDisplayed by booleanPreference(
        AUTHENTICATOR_GET_STARTED_DISPLAYED,
        false
    )

    var cryptoMigrationAttemptDate: Instant?
        get() =
            if (exist(CRYPTO_MIGRATION_ATTEMPT_TIMESTAMP)) {
                Instant.ofEpochMilli(
                getLong(
                    CRYPTO_MIGRATION_ATTEMPT_TIMESTAMP
                )
            )
            } else {
                null
            }
        set(value) {
            if (value == null) {
                remove(CRYPTO_MIGRATION_ATTEMPT_TIMESTAMP)
            } else {
                putLong(
                CRYPTO_MIGRATION_ATTEMPT_TIMESTAMP,
                value.toEpochMilli()
            )
            }
        }

    var biometricSealPaddingMigrationAttempt by booleanPreference(
        BIOMETRIC_SEAL_PADDING_MIGRATION_ATTEMPT,
        false
    )

    var vaultReportLatestTriggerTimestamp: Instant
        get() = Instant.ofEpochSecond(getLong(VAULT_REPORT_LATEST_TRIGGER_TIMESTAMP, 0))
        set(value) {
            putLong(VAULT_REPORT_LATEST_TRIGGER_TIMESTAMP, value.epochSecond)
        }

    var userActivityLastUpdateTimestamp: Instant
        get() = Instant.ofEpochSecond(getLong(USER_ACTIVITY_UPDATE_DATE, 0))
        set(value) {
            putLong(USER_ACTIVITY_UPDATE_DATE, value.epochSecond)
        }

    fun preferencesFor(username: Username): UserPreferencesManager =
        preferencesFor(username.email)

    fun preferencesFor(username: String): UserPreferencesManager =
        CustomUser(context, username)

    fun preferencesForCurrentUser(): UserPreferencesManager? =
        sessionManager?.session?.let { preferencesFor(it.username) }

    class UserLoggedIn(
        private val context: Context,
        private val manager: SessionManager
    ) : UserPreferencesManager(context, manager) {

        override val sharedPreferences: SharedPreferences?
            get() = manager.session?.let { getSharedPreferences(context, it.username.email) }
    }

    private class CustomUser(context: Context, username: String) : UserPreferencesManager(context) {

        override val sharedPreferences: SharedPreferences = getSharedPreferences(context, username)
    }

    companion object {
        private fun getSharedPreferences(context: Context, username: String): SharedPreferences =
            context.getSharedPreferences(MD5Hash.hash(username), Context.MODE_PRIVATE)
    }
}
