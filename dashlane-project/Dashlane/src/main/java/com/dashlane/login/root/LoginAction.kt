package com.dashlane.login.root

import androidx.annotation.StringRes



data class LoginAction(
    @StringRes
    val textResId: Int,
    val onClick: () -> Unit
)