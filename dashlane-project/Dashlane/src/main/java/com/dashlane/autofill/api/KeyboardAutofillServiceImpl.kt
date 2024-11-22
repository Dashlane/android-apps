package com.dashlane.autofill.api

import com.dashlane.autofill.announcement.KeyboardAutofillService
import com.dashlane.preference.PreferencesManager
import com.dashlane.preference.UserPreferencesManager
import com.dashlane.session.SessionManager
import javax.inject.Inject

class KeyboardAutofillServiceImpl @Inject constructor(
    private val sessionManager: SessionManager,
    private val preferencesManager: PreferencesManager
) : KeyboardAutofillService {

    private val userPreferencesManager: UserPreferencesManager
        get() = preferencesManager[sessionManager.session?.username]

    override fun setOnBoardingSuggestionDisplayed() {
        userPreferencesManager.hasSeenKeyboardOnBoardingSuggestion = true
    }

    override fun canDisplayOnBoardingSuggestion() = !userPreferencesManager.hasSeenKeyboardOnBoardingSuggestion &&
            userPreferencesManager.keyboardAutofillAnnouncementTimestamp == 0L
}