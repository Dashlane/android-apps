package com.dashlane.login

import com.dashlane.hermes.generated.definitions.VerificationMode

sealed class LoginMode {
    data object Biometric : LoginMode()

    data object Pin : LoginMode()

    data class MasterPassword(val verification: VerificationMode = VerificationMode.NONE) : LoginMode()

    data object Sso : LoginMode()

    data object SessionRestore : LoginMode()

    data object MasterPasswordChanger : LoginMode()

    data object DeviceTransfer : LoginMode()
}