package com.dashlane.ui

import com.dashlane.debug.services.DaDaDaSecurity
import com.dashlane.preference.ConstantsPrefs
import com.dashlane.preference.GlobalPreferencesManager
import com.dashlane.preference.PreferencesManager
import com.dashlane.session.SessionManager
import javax.inject.Inject

class ScreenshotPolicyImpl @Inject constructor(
    private val sessionManager: SessionManager,
    private val preferencesManager: PreferencesManager,
    private val globalPreferencesManager: GlobalPreferencesManager,
    private val dadadaSecurity: DaDaDaSecurity
) :
    ScreenshotPolicy {

    companion object {
        
        private const val ALLOW_SCREENSHOT_BY_DEFAULT = false
    }

    override fun setScreenshotAllowed(enable: Boolean) {
        preferencesManager[sessionManager.session?.username].putBoolean(ConstantsPrefs.WINDOW_SECURITY_FLAG_DISABLED, enable)
    }

    override fun areScreenshotAllowed(): Boolean {
        if (dadadaSecurity.isScreenshotAllowed || globalPreferencesManager.uiTestForceScreenshot) {
            return true
        }
        return preferencesManager[sessionManager.session?.username]
            .getBoolean(ConstantsPrefs.WINDOW_SECURITY_FLAG_DISABLED, ALLOW_SCREENSHOT_BY_DEFAULT)
    }
}