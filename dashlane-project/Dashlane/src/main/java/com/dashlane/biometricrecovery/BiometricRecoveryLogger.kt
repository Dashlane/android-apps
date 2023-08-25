package com.dashlane.biometricrecovery

interface BiometricRecoveryLogger {
    fun logAccountRecoveryActivation(enabled: Boolean, originViewType: String?)
    fun logBiometricIntroDisplay()
    fun logAccountRecoveryIntroDisplay()
    fun logBiometricRecoveryIntroDialogDisplay()
    fun logPromptBiometricForRecovery(origin: String)
    fun logGoToChangeMP(origin: String)
}