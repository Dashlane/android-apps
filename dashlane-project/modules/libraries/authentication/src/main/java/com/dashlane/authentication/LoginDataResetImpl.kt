package com.dashlane.authentication

import com.dashlane.preference.GlobalPreferencesManager
import com.dashlane.session.SessionTrasher
import com.dashlane.user.Username
import javax.inject.Inject

class LoginDataResetImpl @Inject constructor(
    private val sessionTrasher: SessionTrasher,
    private val globalPreferencesManager: GlobalPreferencesManager,
) : LoginDataReset {

    override suspend fun clearData(username: Username, reason: DataLossTrackingLogger.Reason) {
        sessionTrasher.trash(username = username, deletePreferences = true)
        globalPreferencesManager.setLastLoggedInUser("")
    }
}
