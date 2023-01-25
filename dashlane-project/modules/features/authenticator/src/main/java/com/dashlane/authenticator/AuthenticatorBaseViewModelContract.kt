package com.dashlane.authenticator

import androidx.activity.result.ActivityResultLauncher
import com.dashlane.url.registry.UrlDomainRegistry
import com.dashlane.vault.summary.SummaryObject

interface AuthenticatorBaseViewModelContract {
    val urlDomainRegistry: UrlDomainRegistry
    val isFirstVisit: Boolean
    fun getCredentials(): List<SummaryObject.Authentifiant>
    fun onSeeAll()
    fun onSeeLess()
    fun onSetupAuthenticator(activityResultLauncher: ActivityResultLauncher<Unit?>)
    fun onOtpSetup(itemId: String, otp: Otp)
    fun onSuccessAddOtp(itemId: String?, otp: Otp)
    fun onOnboardingDisplayed()

    sealed class UserCommand {
        data class OtpSetupCommand(
            val itemId: String,
            val otp: Otp?,
            val updateModificationDate: Boolean = true
        ) : UserCommand()

        object SeeAllCommand : UserCommand()
        object SeeLessCommand : UserCommand()
    }

    companion object {
        const val DEFAULT_ITEMS_SHOWN = 5
    }
}