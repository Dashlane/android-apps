package com.dashlane.autofill.changepassword

data class AutoFillChangePasswordConfiguration(
    val filterOnDomain: String? = null,
    val filterOnUsername: String? = null,
    val onItemUpdated: () -> Unit = {},
    val onDomainChanged: () -> Unit = {}
) {
    val isEmpty = filterOnDomain == null && filterOnUsername == null
}