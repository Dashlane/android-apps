package com.dashlane.login

import com.dashlane.user.Username
import com.dashlane.util.installlogs.DataLossTrackingLogger

interface LoginDataReset {
    suspend fun clearData(username: Username, reason: DataLossTrackingLogger.Reason)
}