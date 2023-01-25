package com.dashlane.login

import com.dashlane.account.UserAccountStorage
import com.dashlane.usersupportreporter.UserSupportFileLogger
import javax.inject.Inject

class AccountDataLossTrackingListener @Inject constructor(private val userSupportFileLogger: UserSupportFileLogger) :
    UserAccountStorage.DataLossTrackingListener {
    override fun logUserSupportFile(message: String) {
        userSupportFileLogger.add(message)
    }
}