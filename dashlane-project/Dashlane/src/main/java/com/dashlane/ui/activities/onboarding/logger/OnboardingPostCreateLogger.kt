package com.dashlane.ui.activities.onboarding.logger

import com.dashlane.useractivity.log.install.InstallLogRepository
import com.dashlane.useractivity.log.install.InstallLogCode17

object OnboardingPostCreateLogger {

    @JvmStatic
    fun logInstallLog17(installLogRepository: InstallLogRepository, subStep: String) {
        installLogRepository.enqueue(InstallLogCode17(subStep = subStep))
    }
}