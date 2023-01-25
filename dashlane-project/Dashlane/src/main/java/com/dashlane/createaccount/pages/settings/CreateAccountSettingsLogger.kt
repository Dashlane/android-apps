package com.dashlane.createaccount.pages.settings

import com.dashlane.login.dagger.TrackingId
import com.dashlane.settings.biometric.BiometricSettingsLogger
import com.dashlane.useractivity.log.install.InstallLogCode69
import com.dashlane.useractivity.log.install.InstallLogRepository
import javax.inject.Inject

class CreateAccountSettingsLogger @Inject constructor(
    @TrackingId private val trackingId: String,
    private val installLogRepository: InstallLogRepository
) : BiometricSettingsLogger {
    fun logLand() = log("show")
    fun logBack() = log("back")

    override fun logShowFAQ() = log("showFaq")

    private fun log(action: String, subAction: String? = null) {
        installLogRepository.enqueue(
            InstallLogCode69(
                type = InstallLogCode69.Type.CREATE_ACCOUNT,
                subType = "settingsPage",
                action = action,
                loginSession = trackingId,
                subStep = "11.4",
                subAction = subAction
            )
        )
    }
}