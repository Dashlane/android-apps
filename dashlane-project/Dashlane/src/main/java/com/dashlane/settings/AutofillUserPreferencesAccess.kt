package com.dashlane.settings

import com.dashlane.autofill.AutofillAnalyzerDef
import com.dashlane.preference.UserPreferencesManager
import javax.inject.Inject

class AutofillUserPreferencesAccess @Inject constructor(val userPreferencesManager: UserPreferencesManager) :
    AutofillAnalyzerDef.IUserPreferencesAccess {

    override fun hasKeyboardAutofillEnabled() = userPreferencesManager.hasInlineAutofill
    override fun hasAutomatic2faTokenCopy() = userPreferencesManager.hasAutomatic2faTokenCopy
}