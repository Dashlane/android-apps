package com.dashlane.login.pages.password

import android.content.Intent
import androidx.annotation.StringDef
import com.dashlane.authentication.RegisteredUserDevice
import com.dashlane.hermes.generated.definitions.VerificationMode
import com.dashlane.login.UserAccountStatus



interface LoginPasswordLogger {
    

    fun logLand(allowBypass: Boolean)

    

    fun logBack()

    

    fun logEmptyPassword()

    

    fun logPasswordInvalid()

    

    fun logPasswordInvalidWithRecovery()

    

    fun logNetworkError(@NetworkError error: String)

    

    fun logAccountReset()

    

    fun logPasswordSuccess(origin: Intent)

    

    fun logPasswordVisibilityToggle(shown: Boolean)

    

    fun logLoginIssuesClicked()

    

    fun logLoginIssuesShown()

    

    fun logLoginHelp()

    

    fun logPasswordHelp()

    

    fun logPasswordForgot()

    

    fun logAccountChange()

    

    fun logAccountSwitch()

    

    fun logUserStatus(userAccountStatus: UserAccountStatus, anonymousDeviceId: String)

    

    fun logAskMasterPasswordLater()

    

    fun logRegisteredWithBackupToken()

    

    fun logRegisterWithBackupTokenError()

    companion object {
        const val NW_ERR_OFFLINE = "Offline"
        const val NW_ERR_UKI_VALID = "UkiValid"
        const val NW_ERR_INITIAL_SYNC = "InitialSync"
    }

    @Retention(AnnotationRetention.SOURCE)
    @StringDef(
        NW_ERR_OFFLINE,
        NW_ERR_UKI_VALID,
        NW_ERR_INITIAL_SYNC
    )
    annotation class NetworkError

    interface Factory {
        fun create(
            registeredUserDevice: RegisteredUserDevice,
            verification: VerificationMode
        ): LoginPasswordLogger
    }
}