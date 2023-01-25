package com.dashlane.accountrecovery

interface AccountRecoveryLogger {
    fun logAccountRecoveryActivation(enabled: Boolean, originViewType: String?)
    fun logBiometricIntroDisplay()
    fun logAccountRecoveryIntroDisplay()
    fun logAccountRecoveryIntroDialogDisplay()
    fun logPromptBiometricForRecovery(origin: String)
    fun logGoToChangeMP(origin: String)
}