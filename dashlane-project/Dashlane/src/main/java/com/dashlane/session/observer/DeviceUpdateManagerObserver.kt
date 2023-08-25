package com.dashlane.session.observer

import com.dashlane.device.DeviceUpdateManager
import com.dashlane.login.LoginInfo
import com.dashlane.session.Session
import com.dashlane.session.SessionObserver

class DeviceUpdateManagerObserver(
    private val deviceUpdateManager: DeviceUpdateManager
) : SessionObserver {

    override suspend fun sessionStarted(session: Session, loginInfo: LoginInfo?) {
        deviceUpdateManager.updateIfNeeded()
    }
}