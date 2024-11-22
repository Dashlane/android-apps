package com.dashlane.session.observer

import android.os.Build
import com.dashlane.login.LoginInfo
import com.dashlane.preference.PreferencesManager
import com.dashlane.session.Session
import com.dashlane.session.SessionObserver

class SystemUpdateObserver(private val preferencesManager: PreferencesManager) : SessionObserver {
    override suspend fun sessionStarted(session: Session, loginInfo: LoginInfo?) {
        val currentVersion = Build.VERSION.SDK_INT
        val preferences = preferencesManager[session.username]
        val lastVersion = preferences.lastOsVersion

        
        if (Build.VERSION_CODES.R in (lastVersion + 1)..currentVersion) {
            preferences.requestDisplayKeyboardAnnouncement = true
        }

        preferences.lastOsVersion = currentVersion
    }
}