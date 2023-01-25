package com.dashlane.ui

import com.dashlane.preference.ConstantsPrefs
import com.dashlane.preference.GlobalPreferencesManager
import com.dashlane.preference.UserPreferencesManager
import com.dashlane.debug.DaDaDa
import javax.inject.Inject



class ScreenshotPolicyImpl @Inject constructor(
    private val userPreferencesManager: UserPreferencesManager,
    private val globalPreferencesManager: GlobalPreferencesManager,
    private val dadada: DaDaDa
) :
    ScreenshotPolicy {

    companion object {
        
        private const val ALLOW_SCREENSHOT_BY_DEFAULT = false
    }

    override fun setScreenshotAllowed(enable: Boolean) {
        userPreferencesManager.putBoolean(ConstantsPrefs.WINDOW_SECURITY_FLAG_DISABLED, enable)
    }

    override fun areScreenshotAllowed(): Boolean {
        if (dadada.isScreenshotAllowed || globalPreferencesManager.uiTestForceScreenshot) {
            return true
        }
        return userPreferencesManager
            .getBoolean(ConstantsPrefs.WINDOW_SECURITY_FLAG_DISABLED, ALLOW_SCREENSHOT_BY_DEFAULT)
    }
}