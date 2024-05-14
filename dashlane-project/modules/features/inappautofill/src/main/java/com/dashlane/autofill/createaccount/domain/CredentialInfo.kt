package com.dashlane.autofill.createaccount.domain

data class CredentialInfo(
    val title: String?,
    val website: String?,
    val login: String,
    val password: String,
    val packageName: String?,
    val spaceId: String?
)