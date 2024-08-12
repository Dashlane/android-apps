package com.dashlane.login.root

import com.dashlane.authentication.RegisteredUserDevice
import com.dashlane.login.LoginStrategy
import com.dashlane.login.lock.LockSetting

data class LocalLoginState(
    val registeredUserDevice: RegisteredUserDevice? = null,
    val lockSetting: LockSetting? = null,
)

sealed class LocalLoginNavigationState {

    data class Success(val strategy: LoginStrategy.Strategy?) : LocalLoginNavigationState()
    data class ChangeAccount(val email: String?) : LocalLoginNavigationState()
    data object Cancel : LocalLoginNavigationState()
    data class Logout(val email: String?) : LocalLoginNavigationState()
}