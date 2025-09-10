package com.dashlane.sso.login

import android.net.Uri
import com.dashlane.authentication.sso.utils.UserSsoInfo
import com.dashlane.mvvm.State

sealed class RemoteLoginSsoState : State {
    data class View(
        val loginSsoInfo: LoginSsoInfo? = null,
        val userSsoInfo: UserSsoInfo? = null,
        val migrateToMasterPasswordUser: Boolean = false,
        val termsOfServiceShown: Boolean = false,
        val termsOfServicesAccepted: Boolean = false,
        val privacyPolicyAccepted: Boolean = false,
    ) : RemoteLoginSsoState(), State.View

    sealed class SideEffect : RemoteLoginSsoState(), State.SideEffect {
        data class StartSso(val loginSsoInfo: LoginSsoInfo) : SideEffect()

        data class OpenHelp(val uri: Uri) : SideEffect()

        data object GoToInitialSync : SideEffect()

        data object GoToHome : SideEffect()

        data object GoToOnboardingLock : SideEffect()

        data class GoToMigrationToMasterPasswordUser(
            val authTicket: String,
            val shouldLaunchInitialSync: Boolean,
            val hasPinOrBiometric: Boolean,
        ) : SideEffect()

        data class FinishWithResult(val result: RemoteLoginSsoActivity.Result) : SideEffect()

        data object Cancel : SideEffect()
    }
}

data class LoginSsoInfo(
    val email: String,
    val serviceProviderUrl: String,
    val isNitroProvider: Boolean = false,
)