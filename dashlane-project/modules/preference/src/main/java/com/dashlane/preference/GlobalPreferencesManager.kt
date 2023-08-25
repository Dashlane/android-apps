package com.dashlane.preference

import android.app.backup.BackupManager
import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.dashlane.preference.ConstantsPrefs.Companion.AUTOFILL_NOTIFICATION_DISMISS_COUNT
import com.dashlane.preference.ConstantsPrefs.Companion.BACKUP_TOKEN_SET
import com.dashlane.preference.ConstantsPrefs.Companion.DEVICE_COUNTRY
import com.dashlane.preference.ConstantsPrefs.Companion.DEVICE_COUNTRY_REFRESH
import com.dashlane.preference.ConstantsPrefs.Companion.DEVICE_EUROPEAN_UNION_STATUS
import com.dashlane.preference.ConstantsPrefs.Companion.HAS_ACTIVATED_AUTOFILL_ONCE
import com.dashlane.preference.ConstantsPrefs.Companion.INITIAL_RUN_FINISHED
import com.dashlane.preference.ConstantsPrefs.Companion.INSTALLATION_TRACKING_ID
import com.dashlane.preference.ConstantsPrefs.Companion.IS_FIRST_PASSWORD_MANAGER_LAUNCH_LOG_SENT
import com.dashlane.preference.ConstantsPrefs.Companion.IS_USER_LOCKED
import com.dashlane.preference.ConstantsPrefs.Companion.LOGGED_USER
import com.dashlane.preference.ConstantsPrefs.Companion.MULTIPLE_ACCOUNT_LOADED_ON_THIS_DEVICE
import com.dashlane.preference.ConstantsPrefs.Companion.REGISTRATION_ID
import com.dashlane.preference.ConstantsPrefs.Companion.SKIP_INTRO
import com.dashlane.preference.ConstantsPrefs.Companion.UITEST_FORCE_SCREENSHOT
import com.dashlane.preference.ConstantsPrefs.Companion.USER_LIST_HISTORY
import com.dashlane.preference.ConstantsPrefs.Companion.USER_LIST_HISTORY_MAX_SIZE
import com.dashlane.session.Username
import com.dashlane.util.isNotSemanticallyNull
import com.dashlane.util.tryOrNull
import java.io.File
import java.time.Instant
import java.util.UUID

class GlobalPreferencesManager(
    private val context: Context,
    private val backupManager: BackupManager
) : DashlanePreferencesManager() {

    override val sharedPreferences: SharedPreferences? = PreferenceManager.getDefaultSharedPreferences(context)
    val lastModificationTime: Long
        get() {
            val packageName = context.packageName
            return tryOrNull {
                File(
                    context.filesDir.parent,
                    "/shared_prefs/${packageName}_preferences.xml"
                ).lastModified()
            } ?: 0L
        }

    var uiTestForceScreenshot by booleanPreference(UITEST_FORCE_SCREENSHOT)
    var isInitialRunFinished by booleanPreference(INITIAL_RUN_FINISHED)
    var isUserLoggedOut by booleanPreference(IS_USER_LOCKED)
    var isFirstPasswordManagerLogSent by booleanPreference(IS_FIRST_PASSWORD_MANAGER_LAUNCH_LOG_SENT)

    var isMultipleAccountLoadedOnThisDevice by booleanPreference(MULTIPLE_ACCOUNT_LOADED_ON_THIS_DEVICE)

    var fcmRegistrationId by stringPreference(REGISTRATION_ID)

    val installationTrackingId: String
        get() = getString(INSTALLATION_TRACKING_ID)
            ?: UUID.randomUUID().toString().also { putString(INSTALLATION_TRACKING_ID, it) }

    var allowSendLogs by booleanPreference(ConstantsPrefs.ALLOW_SEND_LOGS, defaultValue = true)

    fun getBackupTokenDate(username: Username) =
        getBackupTokenDate(username.email)

    fun getBackupTokenDate(username: String) =
        Instant.ofEpochSecond(getBackupToken(username)?.get(2)?.toLongOrNull() ?: 0L)

    fun getCipheredBackupToken(username: Username) =
        getCipheredBackupToken(username.email)

    fun getCipheredBackupToken(username: String) =
        getBackupToken(username)?.get(1)

    fun setCipheredBackupToken(username: Username, value: String, creationDate: Instant = Instant.now()) {
        val tokenByUsername = getStringSet(BACKUP_TOKEN_SET)?.toMutableList() ?: mutableListOf()
        tokenByUsername.apply {
            removeAll { it.startsWith(username.email) }
            add("${username.email};;$value;;${creationDate.epochSecond}")
        }
        putStringSet(BACKUP_TOKEN_SET, tokenByUsername.toSet())
    }

    fun deleteBackupToken(username: Username) {
        getStringSet(BACKUP_TOKEN_SET)?.toMutableList()?.apply {
            removeAll { it.startsWith(username.email) }
            putStringSet(BACKUP_TOKEN_SET, toSet())
        }
    }

    fun saveSkipIntro() {
        putBoolean(SKIP_INTRO, true)
        backupManager.dataChanged()
    }

    fun shouldSkipIntro(): Boolean {
        return getBoolean(SKIP_INTRO)
    }

    fun setLastLoggedInUser(username: String) {
        val lastUser = getLastLoggedInUser()
        if (!lastUser.isEmpty()) {
            isUserLoggedOut = false
        }
        if (lastUser.isNotSemanticallyNull() &&
            lastUser != username &&
            !getBoolean(MULTIPLE_ACCOUNT_LOADED_ON_THIS_DEVICE)
        ) {
            putBoolean(MULTIPLE_ACCOUNT_LOADED_ON_THIS_DEVICE, true)
        }

        val userList = getUserListHistory().toMutableList()
        userList.remove(username)
        userList.add(0, username)
        putList(USER_LIST_HISTORY, userList.take(USER_LIST_HISTORY_MAX_SIZE))
        putString(LOGGED_USER, username)
        backupManager.dataChanged()
    }

    fun getLastLoggedInUser(): String {
        val result = getUserListHistory().firstOrNull()
        if (result == null || result.isBlank()) {
            return ""
        }
        return result
    }

    fun getDefaultUsername(): Username? {
        val lastLoggedInUser = getLastLoggedInUser()
        return Username.ofEmailOrNull(lastLoggedInUser)
    }

    fun getUserListHistory(): List<String> =
        getList(USER_LIST_HISTORY) ?: listOfNotNull(getString(LOGGED_USER))

    fun getDeviceCountry(): String? {
        return getString(DEVICE_COUNTRY)
    }

    fun setDeviceCountry(deviceCountry: String) {
        putString(DEVICE_COUNTRY, deviceCountry)
        putLong(DEVICE_COUNTRY_REFRESH, System.currentTimeMillis())
    }

    fun getDeviceInEuropeanUnion(): Boolean {
        return getBoolean(DEVICE_EUROPEAN_UNION_STATUS, true)
    }

    fun setDeviceInEuropeanUnion(isInEuropeanUnion: Boolean) {
        putBoolean(DEVICE_EUROPEAN_UNION_STATUS, isInEuropeanUnion)
    }

    fun getDeviceCountryRefreshTimestamp(): Long {
        return getLong(DEVICE_COUNTRY_REFRESH)
    }

    fun getAutofillNotificationDismissCount() = getInt(AUTOFILL_NOTIFICATION_DISMISS_COUNT)

    fun incrementAutofillNotificationDismiss() {
        putInt(AUTOFILL_NOTIFICATION_DISMISS_COUNT, getAutofillNotificationDismissCount() + 1)
    }

    fun saveActivatedAutofillOnce() {
        putBoolean(HAS_ACTIVATED_AUTOFILL_ONCE, true)
    }

    fun hasActivatedAutofillOnce(): Boolean = getBoolean(HAS_ACTIVATED_AUTOFILL_ONCE, false)

    fun count() = sharedPreferences?.let { it.all.keys.size } ?: -1

    private fun getBackupToken(username: String) =
        getStringSet(BACKUP_TOKEN_SET)?.firstOrNull { it.startsWith(username) }?.split(";;")
}
