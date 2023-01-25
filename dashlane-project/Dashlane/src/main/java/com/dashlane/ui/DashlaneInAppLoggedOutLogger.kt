package com.dashlane.ui

import com.dashlane.useractivity.log.install.InstallLogRepository
import com.dashlane.useractivity.log.install.InstallLogCode42



class DashlaneInAppLoggedOutLogger(private val installLogRepository: InstallLogRepository) {

    var packageName: String? = null

    fun onClickNotLogin() {
        logLogin(InstallLogCode42.Action.LOGIN_NO)
    }

    fun onClickLogin() {
        logLogin(InstallLogCode42.Action.LOGIN_YES)
    }

    private fun logLogin(action: InstallLogCode42.Action) {
        installLogRepository.enqueue(
            InstallLogCode42(
                appPackage = packageName,
                sender = InstallLogCode42.Sender.DASHLANE,
                action = action
            )
        )
    }
}