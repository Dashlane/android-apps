package com.dashlane.login.sso

import android.os.Parcelable
import com.dashlane.useractivity.log.install.InstallLogCode69
import com.dashlane.useractivity.log.usage.UsageLogCode2
import kotlinx.parcelize.Parcelize



interface LoginSsoLogger {
    fun logLoginStart()

    fun logLoginSuccess()

    fun logInvalidSso()

    fun logErrorUnknown()

    fun logGetUserSsoInfoSuccess()

    fun logGetUserSsoInfoCancel()

    fun logAccountCreationStart()

    fun logNotProvisionedDisplay()

    @Parcelize
    data class Config(
        val trackingId: String,
        val installLogCode69Type: InstallLogCode69.Type,
        val usageLogCode2AppWebsite: String?,
        val usageLogCode2Sender: UsageLogCode2.Sender
    ) : Parcelable {

        companion object {
            

            const val INTENT_EXTRA_KEY = "logger_config"
        }
    }
}