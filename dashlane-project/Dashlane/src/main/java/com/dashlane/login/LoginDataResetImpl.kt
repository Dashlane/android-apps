package com.dashlane.login

import com.dashlane.preference.GlobalPreferencesManager
import com.dashlane.session.SessionTrasher
import com.dashlane.user.Username
import com.dashlane.util.installlogs.DataLossTrackingLogger
import javax.inject.Inject

class LoginDataResetImpl @Inject constructor(
    private val sessionTrasher: SessionTrasher,
    private val globalPreferencesManager: GlobalPreferencesManager,
) : LoginDataReset {

    override suspend fun clearData(username: Username, reason: DataLossTrackingLogger.Reason) {
        sessionTrasher.trash(username)
        globalPreferencesManager.setLastLoggedInUser("")
    }
}
