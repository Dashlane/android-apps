package com.dashlane.session.observer

import com.dashlane.autofill.phishing.AntiPhishingFilesDownloader
import com.dashlane.inapplogin.InAppLoginManager
import com.dashlane.login.LoginInfo
import com.dashlane.session.Session
import com.dashlane.session.SessionObserver
import javax.inject.Inject

class AntiPhishingObserver @Inject constructor(
    private val antiPhishingFilesDownloader: AntiPhishingFilesDownloader,
    private val inAppLoginManager: InAppLoginManager,
) : SessionObserver {
    override suspend fun sessionStarted(session: Session, loginInfo: LoginInfo?) {
        if (inAppLoginManager.isEnableForApp()) {
            antiPhishingFilesDownloader.downloadFilePhishingFiles()
        }
    }
}