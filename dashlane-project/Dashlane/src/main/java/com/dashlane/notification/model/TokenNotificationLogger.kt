package com.dashlane.notification.model

import com.dashlane.useractivity.log.install.InstallLogRepository
import com.dashlane.useractivity.log.install.InstallLogCode30



class TokenNotificationLogger(private val installLogRepository: InstallLogRepository) {

    fun logShowToken() {
        installLogRepository.enqueue(InstallLogCode30(subStep = "0"))
    }

    fun logShowDialogWithoutToken() {
        installLogRepository.enqueue(InstallLogCode30(subStep = "1"))
    }
}