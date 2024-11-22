package com.dashlane.authentication

import com.dashlane.user.Username

fun interface LoginDataReset {
    suspend fun clearData(username: Username, reason: DataLossTrackingLogger.Reason)
}