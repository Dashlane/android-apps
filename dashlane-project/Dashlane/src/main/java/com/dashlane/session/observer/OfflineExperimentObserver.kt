package com.dashlane.session.observer

import com.dashlane.abtesting.OfflineExperimentReporter
import com.dashlane.login.LoginInfo
import com.dashlane.session.Session
import com.dashlane.session.SessionObserver
import com.dashlane.session.authorization

class OfflineExperimentObserver(private val offlineExperimentReporter: OfflineExperimentReporter) : SessionObserver {

    override suspend fun sessionStarted(session: Session, loginInfo: LoginInfo?) {
        offlineExperimentReporter.reportIfNeeded(session.authorization)
    }
}