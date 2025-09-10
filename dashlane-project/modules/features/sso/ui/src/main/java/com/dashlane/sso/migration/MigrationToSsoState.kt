package com.dashlane.sso.migration

import com.dashlane.authentication.sso.GetSsoInfoResult
import com.dashlane.mvvm.State
import com.dashlane.sso.login.LoginSsoInfo
import com.dashlane.udf.Event

data class MigrationToSsoData(
    val login: String?,
    val serviceProviderUrl: String?,
    val isNitroProvider: Boolean,
    val totpAuthTicket: String?,
    val isLoading: Boolean = false,
    val error: MigrationToSsoError? = null,
) : State.View

sealed class MigrationToSsoState : State {
    sealed class View : MigrationToSsoState(), State.View {
        abstract val data: MigrationToSsoData

        data class Initial(override val data: MigrationToSsoData) : View()

        data class Logout(override val data: MigrationToSsoData) : View()

        data class LoadingSsoLogin(override val data: MigrationToSsoData) : View()

        data class LoadingMigration(override val data: MigrationToSsoData, val result: GetSsoInfoResult?) : View()

        data class Error(override val data: MigrationToSsoData) : View()
    }

    sealed class SideEffect : MigrationToSsoState(), State.SideEffect {
        data object Cancel : SideEffect()

        data class GoToSso(val loginSsoInfo: LoginSsoInfo) : SideEffect()

        data class OpenLink(val url: String) : SideEffect()

        data class Finish(val shouldLaunchInitialSync: Boolean, val goToApplicationLock: Boolean) : SideEffect()

        data class ShowError(val error: MigrationToSsoError) : SideEffect()
    }
}

sealed class MigrationToSsoEvent : Event {
    data class Error(val error: MigrationToSsoError) : MigrationToSsoEvent()

    data object Cancel : MigrationToSsoEvent()

    data object Continue : MigrationToSsoEvent()

    data class LinkClicked(val url: String) : MigrationToSsoEvent()

    data class SsoCompleted(val result: GetSsoInfoResult?) : MigrationToSsoEvent()

    data class Success(val shouldLaunchInitialSync: Boolean, val goToApplicationLock: Boolean) : MigrationToSsoEvent()
}

sealed class MigrationToSsoError : State.SideEffect {
    data object Network : MigrationToSsoError()

    data object Generic : MigrationToSsoError()
}