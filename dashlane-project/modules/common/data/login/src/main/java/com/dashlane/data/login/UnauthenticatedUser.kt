package com.dashlane.data.login

data class UnauthenticatedUser(
    val email: String,
    val cipheredBackupToken: CipheredBackupToken? = null,
)