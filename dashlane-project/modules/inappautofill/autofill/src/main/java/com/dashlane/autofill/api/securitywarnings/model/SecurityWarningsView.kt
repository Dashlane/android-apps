package com.dashlane.autofill.api.securitywarnings.model

import com.dashlane.autofill.api.unlockfill.UnlockedAuthentifiant

internal interface SecurityWarningsView {
    fun finish()
    fun autoAcceptUnknown(): Boolean
    fun autoAcceptMismatch(): Boolean
    fun showIncorrectWarning(unlockedAuthentifiant: UnlockedAuthentifiant)
    fun showMismatchWarning(unlockedAuthentifiant: UnlockedAuthentifiant)
    fun showUnknownWarning(unlockedAuthentifiant: UnlockedAuthentifiant)
    fun finishWithResult(
        unlockedAuthentifiant: UnlockedAuthentifiant,
        showWarningRemembered: Boolean,
        warningShown: Boolean
    )
}
