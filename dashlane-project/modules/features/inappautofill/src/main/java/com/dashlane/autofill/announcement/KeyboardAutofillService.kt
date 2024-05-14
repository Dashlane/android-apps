package com.dashlane.autofill.announcement

interface KeyboardAutofillService {

    fun setOnBoardingSuggestionDisplayed()

    fun canDisplayOnBoardingSuggestion(): Boolean
}