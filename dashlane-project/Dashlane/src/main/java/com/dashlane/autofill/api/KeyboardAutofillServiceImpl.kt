package com.dashlane.autofill.api

import com.dashlane.autofill.announcement.KeyboardAutofillService
import com.dashlane.preference.UserPreferencesManager
import javax.inject.Inject

class KeyboardAutofillServiceImpl @Inject constructor(
    private val userPreferencesManager: UserPreferencesManager
) : KeyboardAutofillService {

    override fun setOnBoardingSuggestionDisplayed() {
        userPreferencesManager.hasSeenKeyboardOnBoardingSuggestion = true
    }

    override fun canDisplayOnBoardingSuggestion() = !userPreferencesManager.hasSeenKeyboardOnBoardingSuggestion &&
            userPreferencesManager.keyboardAutofillAnnouncementTimestamp == 0L
}