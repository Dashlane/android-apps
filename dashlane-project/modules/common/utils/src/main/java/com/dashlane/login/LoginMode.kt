package com.dashlane.login

import com.dashlane.hermes.generated.definitions.Mode
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

val LoginMode.verification get() = if (this is LoginMode.MasterPassword) verification else null

fun LoginMode.toMode(): Mode? = when (this) {
    LoginMode.Biometric -> Mode.BIOMETRIC
    LoginMode.Pin -> Mode.PIN
    is LoginMode.MasterPassword -> Mode.MASTER_PASSWORD
    LoginMode.Sso -> Mode.SSO
    LoginMode.DeviceTransfer -> Mode.DEVICE_TRANSFER
    LoginMode.SessionRestore,
    LoginMode.MasterPasswordChanger -> null
}