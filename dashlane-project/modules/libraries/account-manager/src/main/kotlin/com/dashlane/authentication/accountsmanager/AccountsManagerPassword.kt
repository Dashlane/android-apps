package com.dashlane.authentication.accountsmanager

data class AccountsManagerPassword(
    val data: String,
    val isLocalKey: Boolean,
    val serverKey: String?
)