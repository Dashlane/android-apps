package com.dashlane.login.pages.email

import androidx.annotation.StringDef
import com.dashlane.account.UserSecuritySettings



interface LoginEmailLogger {
    

    fun logLand(@LandState state: String, securitySettings: UserSecuritySettings?)

    

    fun logBack()

    

    fun logAutoFill(securitySettings: UserSecuritySettings?)

    

    fun logEmptyEmail()

    

    fun logInvalidEmail()

    

    fun logRejectedEmail(invalid: Boolean)

    

    fun logNetworkError(@InputType inputType: String, @NetworkError error: String)

    

    fun logValidatedEmail(
        registeredDevice: Boolean,
        @InputType inputType: String,
        securitySettings: UserSecuritySettings?
    )

    

    fun logClearedEmail(securitySettings: UserSecuritySettings?)

    

    fun logCreateAccountClick()

    companion object {
        const val LAND_FIRST_LOGIN = "firstLogin"
        const val LAND_PRE_FILL = "preFill"
        const val LAND_AUTO_FILL = "autoFill"

        const val INPUT_MANUAL = "nextManual"
        const val INPUT_AUTO_FILL = "nextSuggested"
        const val INPUT_PRE_FILL = "nextPreFill"

        const val NW_ERR_OFFLINE = "Offline"
        const val NW_ERR_ACCOUNT_SETTINGS = "AccountSettings"
        const val NW_ERR_CHECK_DELETION = "CheckDeletion"
        const val NW_ERR_SEND_TOKEN = "SendToken"
    }

    @Retention(AnnotationRetention.SOURCE)
    @StringDef(
        LAND_FIRST_LOGIN,
        LAND_PRE_FILL,
        LAND_AUTO_FILL
    )
    annotation class LandState

    @Retention(AnnotationRetention.SOURCE)
    @StringDef(
        INPUT_MANUAL,
        INPUT_PRE_FILL,
        INPUT_AUTO_FILL
    )
    annotation class InputType

    @Retention(AnnotationRetention.SOURCE)
    @StringDef(
        NW_ERR_OFFLINE,
        NW_ERR_ACCOUNT_SETTINGS,
        NW_ERR_CHECK_DELETION,
        NW_ERR_SEND_TOKEN
    )
    annotation class NetworkError
}