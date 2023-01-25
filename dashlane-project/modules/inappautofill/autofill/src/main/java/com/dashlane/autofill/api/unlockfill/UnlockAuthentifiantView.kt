package com.dashlane.autofill.api.unlockfill

internal interface UnlockAuthentifiantView {
    fun isFirstRun(): Boolean
    fun canRequestLockScreen(): Boolean
    fun finishWithAutoFillSuggestions()
    fun startLockActivity()
    fun finish()
    fun authentifiantItemUnlocked(unlockedAuthentifiant: UnlockedAuthentifiant)
}