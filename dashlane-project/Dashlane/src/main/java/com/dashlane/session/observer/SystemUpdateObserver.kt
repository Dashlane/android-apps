package com.dashlane.session.observer

import android.os.Build
import com.dashlane.login.LoginInfo
import com.dashlane.preference.UserPreferencesManager
import com.dashlane.session.Session
import com.dashlane.session.SessionObserver

class SystemUpdateObserver(private val userPreferencesManager: UserPreferencesManager) : SessionObserver {
    override suspend fun sessionStarted(session: Session, loginInfo: LoginInfo?) {
        val currentVersion = Build.VERSION.SDK_INT
        val lastVersion = userPreferencesManager.lastOsVersion

        hasUpdatedToAndroidR(lastVersion, currentVersion)

        updateLastOsVersion(currentVersion)
    }

    private fun hasUpdatedToAndroidR(lastVersion: Int, currentVersion: Int) {
        
        if (Build.VERSION_CODES.R in (lastVersion + 1)..currentVersion) {
            userPreferencesManager.requestDisplayKeyboardAnnouncement = true
        }
    }

    private fun updateLastOsVersion(current: Int) {
        userPreferencesManager.lastOsVersion = current
    }
}