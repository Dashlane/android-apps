package com.dashlane.session.observer

import com.dashlane.autofill.phishing.cleaner.AntiPhishingFilesCleaner
import com.dashlane.data.login.LoginInfo
import com.dashlane.session.Session
import com.dashlane.session.SessionObserver
import javax.inject.Inject

class AntiPhishingObserver @Inject constructor(
    private val antiPhishingFilesCleaner: AntiPhishingFilesCleaner,
) : SessionObserver {
    override suspend fun sessionStarted(session: Session, loginInfo: LoginInfo?) {
        antiPhishingFilesCleaner.cleanPhishdroidFiles()
    }
}