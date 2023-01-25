package com.dashlane.login

import com.dashlane.preference.GlobalPreferencesManager
import com.dashlane.session.SessionTrasher
import com.dashlane.session.Username
import com.dashlane.useractivity.log.install.InstallLogRepository
import com.dashlane.usersupportreporter.UserSupportFileLogger
import com.dashlane.util.installlogs.DataLossTrackingLogger
import javax.inject.Inject

class LoginDataResetImpl @Inject constructor(
    private val userSupportFileLogger: UserSupportFileLogger,
    private val sessionTrasher: SessionTrasher,
    private val globalPreferencesManager: GlobalPreferencesManager,
    private val installLogRepository: InstallLogRepository
) : LoginDataReset {

    override suspend fun clearData(username: Username, reason: DataLossTrackingLogger.Reason) {
        userSupportFileLogger.add("ClearData Logout, for $username, reason: $reason")
        DataLossTrackingLogger(installLogRepository).log(reason)
        sessionTrasher.trash(username)
        globalPreferencesManager.setLastLoggedInUser("")
    }
}