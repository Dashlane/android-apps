package com.dashlane.autofill.api.securitywarnings

import com.dashlane.autofill.securitywarnings.data.JsonPreferencesRememberSecurityWarningsRepository
import com.dashlane.autofill.securitywarnings.data.SecurityWarningsPreferencesManagerWrapper
import com.dashlane.autofill.securitywarnings.model.RememberSecurityWarningsRepository
import com.dashlane.preference.ConstantsPrefs
import com.dashlane.preference.UserPreferencesManager
import javax.inject.Inject

class UserPreferencesRememberSecurityWarningsJsonRepository @Inject constructor(
    private val userPreferencesManager: UserPreferencesManager
) : RememberSecurityWarningsRepository by JsonPreferencesRememberSecurityWarningsRepository(
    PreferencesWrapper(userPreferencesManager)
) {
    private class PreferencesWrapper(val userPreferencesManager: UserPreferencesManager) :
        SecurityWarningsPreferencesManagerWrapper {
        override val incorrectJsonKey: String = ConstantsPrefs.AUTOFILL_REMEMBER_SECURITY_WARNINGS_INCORRECT_JSON
        override val unknownJsonKey: String = ConstantsPrefs.AUTOFILL_REMEMBER_SECURITY_WARNINGS_UNKNOWN_JSON

        override fun remove(key: String): Boolean = userPreferencesManager.remove(key)

        override fun getString(key: String): String? = userPreferencesManager.getString(key)

        override fun putString(key: String, stringSet: String): Boolean =
            userPreferencesManager.putString(key, stringSet)
    }
}
