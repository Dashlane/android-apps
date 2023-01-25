package com.dashlane.login

data class LoginInfo(
    val isFirstLogin: Boolean,
    val loginMode: LoginMode?
)