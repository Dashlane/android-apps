package com.dashlane.login.sso

import com.dashlane.data.login.SsoInfo
import com.dashlane.lock.LockSetting
import com.dashlane.mvvm.State
import com.dashlane.user.UserAccountInfo

data class LoginSsoState(
    val userAccountInfo: UserAccountInfo? = null,
    val loginHistory: List<String> = emptyList(),
    val lockSetting: LockSetting? = null,
    val isLoading: Boolean = false,
    val error: LoginSsoError? = null,
) : State

sealed class LoginSsoNavigationState : State {
    data class GoToSso(val ssoInfo: SsoInfo) : LoginSsoNavigationState()

    data object UnlockSuccess : LoginSsoNavigationState()

    data object Cancel : LoginSsoNavigationState()

    data class ChangeAccount(val email: String?) : LoginSsoNavigationState()
}

sealed class LoginSsoError {
    data object Generic : LoginSsoError()

    data object Offline : LoginSsoError()

    data object Network : LoginSsoError()

    data object AccountNotFound : LoginSsoError()

    data object InvalidSso : LoginSsoError()

    data object ForbiddenNavigation : LoginSsoError()

    data object SamlResponseNotFound : LoginSsoError()

    data object CannotOpenServiceProvider : LoginSsoError()
}