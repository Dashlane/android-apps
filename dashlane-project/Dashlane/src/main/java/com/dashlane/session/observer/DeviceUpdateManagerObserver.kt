package com.dashlane.session.observer

import android.content.Context
import com.dashlane.accountrecovery.AccountRecovery
import com.dashlane.crashreport.CrashReporter
import com.dashlane.device.DeviceInformationGenerator
import com.dashlane.device.DeviceUpdateManager
import com.dashlane.inapplogin.InAppLoginManager
import com.dashlane.login.LoginInfo
import com.dashlane.session.Session
import com.dashlane.session.SessionObserver
import com.dashlane.storage.DataStorageProvider



class DeviceUpdateManagerObserver(
    private val context: Context,
    private val deviceUpdateManager: DeviceUpdateManager,
    private val crashReporter: CrashReporter,
    private val accountRecovery: AccountRecovery,
    private val inAppLoginManager: InAppLoginManager,
    private val dataStorageProvider: DataStorageProvider
) : SessionObserver {

    override suspend fun sessionStarted(session: Session, loginInfo: LoginInfo?) {
        deviceUpdateManager.updateIfNeeded(
            session.userId, session.uki,
            DeviceInformationGenerator(
                context,
                crashReporter,
                accountRecovery,
                inAppLoginManager,
                dataStorageProvider
            ).generate()
        )
    }
}