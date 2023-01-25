package com.dashlane.ui.activities.fragments.checklist

import com.dashlane.preference.UserPreferencesManager
import java.time.Duration
import java.time.Instant
import javax.inject.Inject

class ChecklistHelper @Inject constructor(
    private val userPreferencesManager: UserPreferencesManager
) {

    fun shouldDisplayChecklist(): Boolean {
        
        if (hasDismissedChecklist()) {
            return false
        }
        
        return !(isAccountOlderThan7Days() && !hasSeenChecklist())
    }

    fun isAccountOlderThan7Days(): Boolean {
        val accountCreationDate: Instant = getAccountCreationDate()
        return accountCreationDate + Duration.ofDays(7) < Instant.now()
    }

    fun hasSeenChecklist(): Boolean {
        return userPreferencesManager.getBoolean(HAS_SEEN_WELCOME_CHECKLIST_PREF_KEY, false)
    }

    fun setChecklistSeen() {
        userPreferencesManager.putBoolean(HAS_SEEN_WELCOME_CHECKLIST_PREF_KEY, true)
    }

    private fun hasDismissedChecklist(): Boolean {
        return userPreferencesManager.getBoolean(HAS_DISMISSED_WELCOME_CHECKLIST_PREF_KEY, false)
    }

    fun setChecklistDismissed() {
        userPreferencesManager.putBoolean(HAS_DISMISSED_WELCOME_CHECKLIST_PREF_KEY, true)
    }

    private fun getAccountCreationDate(): Instant {
        return userPreferencesManager.accountCreationDate
    }

    companion object {
        private const val HAS_SEEN_WELCOME_CHECKLIST_PREF_KEY = "has_seen_welcome_screen"
        private const val HAS_DISMISSED_WELCOME_CHECKLIST_PREF_KEY = "has_dismissed_welcome_screen"
    }
}