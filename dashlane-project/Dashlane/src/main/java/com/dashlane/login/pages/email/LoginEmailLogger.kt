package com.dashlane.login.pages.email

import androidx.annotation.StringDef

interface LoginEmailLogger {

    fun logEmptyEmail()

    fun logInvalidEmail()

    fun logRejectedEmail(invalid: Boolean)

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
    }

    @Retention(AnnotationRetention.SOURCE)
    @StringDef(
        LAND_FIRST_LOGIN,
        LAND_PRE_FILL,
        LAND_AUTO_FILL
    )
    annotation class LandState
}