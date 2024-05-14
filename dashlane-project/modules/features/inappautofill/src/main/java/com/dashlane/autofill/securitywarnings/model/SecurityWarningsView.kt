package com.dashlane.autofill.securitywarnings.model

import com.dashlane.autofill.unlockfill.UnlockedAuthentifiant

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
