package com.dashlane.login.root

import com.dashlane.authentication.RegisteredUserDevice

data class LoginState(
    val registeredUserDevice: RegisteredUserDevice? = null
)