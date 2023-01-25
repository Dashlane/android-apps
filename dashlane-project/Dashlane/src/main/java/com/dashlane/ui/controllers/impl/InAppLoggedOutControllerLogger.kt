package com.dashlane.ui.controllers.impl

import com.dashlane.useractivity.log.install.InstallLogRepository
import com.dashlane.useractivity.log.install.InstallLogCode42

class InAppLoggedOutControllerLogger(private val installLogRepository: InstallLogRepository) {

    fun log(packageName: String?, action: InstallLogCode42.Action?) {
        installLogRepository.enqueue(
            InstallLogCode42(
                appPackage = packageName,
                sender = InstallLogCode42.Sender.DASHLANE,
                action = action
            )
        )
    }
}