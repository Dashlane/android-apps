package com.dashlane.item.passwordhistory

sealed class PasswordHistoryEntry {
    abstract val password: String
    abstract val lastModifiedDateString: String

    data class CredentialPasswordHistoryEntry(
        val credentialId: String,
        override val password: String,
        override val lastModifiedDateString: String,
    ) : PasswordHistoryEntry()

    data class GeneratedPasswordHistoryEntry(
        override val password: String,
        override val lastModifiedDateString: String,
        val domain: String?,
        val saved: Boolean,
    ) : PasswordHistoryEntry()
}