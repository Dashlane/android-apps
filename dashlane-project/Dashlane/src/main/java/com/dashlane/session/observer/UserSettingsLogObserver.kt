package com.dashlane.session.observer

import com.dashlane.hermes.LogRepository
import com.dashlane.login.LoginInfo
import com.dashlane.session.Session
import com.dashlane.session.SessionObserver
import com.dashlane.ui.screens.settings.UserSettingsLogRepository

class UserSettingsLogObserver(
    private val logRepository: LogRepository,
    private val userSettingsLogRepository: UserSettingsLogRepository
) : SessionObserver {
    override suspend fun sessionStarted(session: Session, loginInfo: LoginInfo?) {
        logRepository.queueEvent(userSettingsLogRepository.get())
    }
}