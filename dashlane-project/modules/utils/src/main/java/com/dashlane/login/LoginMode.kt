package com.dashlane.login

import com.dashlane.hermes.generated.definitions.VerificationMode

sealed class LoginMode {
    object Biometric : LoginMode()

    object Pin : LoginMode()

    data class MasterPassword(val verification: VerificationMode = VerificationMode.NONE) : LoginMode()

    object Sso : LoginMode()

    object SessionRestore : LoginMode()

    object MasterPasswordChanger : LoginMode()
}