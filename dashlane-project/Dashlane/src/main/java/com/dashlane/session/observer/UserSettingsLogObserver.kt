package com.dashlane.session.observer

import android.content.Context
import com.dashlane.hermes.inject.HermesComponent
import com.dashlane.login.LoginInfo
import com.dashlane.session.Session
import com.dashlane.session.SessionObserver
import com.dashlane.ui.screens.settings.UserSettingsLogRepository



class UserSettingsLogObserver(private val context: Context) : SessionObserver {
    override suspend fun sessionStarted(session: Session, loginInfo: LoginInfo?) {
        HermesComponent(context).logRepository.queueEvent(UserSettingsLogRepository().get())
    }
}