package com.dashlane.vault.model

import com.dashlane.xml.domain.SyncObject

val SyncObject.Identity.fullName: String
    get() {
        val firstname = firstName?.takeIf { it.isNotSemanticallyNull() } ?: ""
        val lastname = lastName?.takeIf { it.isNotSemanticallyNull() } ?: ""
        val login = pseudo?.takeIf { it.isNotSemanticallyNull() } ?: ""
        val value = "$firstname $lastname"
        return when {
            value.isNotBlank() -> value.trim()
            login.isNotSemanticallyNull() -> login
            else -> ""
        }
    }