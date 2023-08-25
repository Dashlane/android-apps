package com.dashlane.storage.userdata

interface EmailSuggestionProvider {
    fun getAllEmails(): List<String>
}