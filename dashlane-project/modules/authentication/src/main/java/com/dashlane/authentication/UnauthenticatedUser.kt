package com.dashlane.authentication

data class UnauthenticatedUser(
    val email: String,
    val cipheredBackupToken: CipheredBackupToken? = null
)