package com.dashlane.welcome

import com.dashlane.useractivity.log.install.InstallLogCode17
import com.dashlane.useractivity.log.install.InstallLogRepository
import javax.inject.Inject

class WelcomeLogger @Inject constructor(private val installLogRepository: InstallLogRepository) {
    fun logDisplayed() = log17("91.1.0")

    fun logLoginClicked() = log17("91.1.1")

    fun logGetStartedClicked() = log17("91.1.2")

    fun logPageDisplayed(position: Int) = log17("91.1.3.$position")

    private fun log17(code: String) {
        installLogRepository.enqueue(InstallLogCode17(subStep = code))
    }
}