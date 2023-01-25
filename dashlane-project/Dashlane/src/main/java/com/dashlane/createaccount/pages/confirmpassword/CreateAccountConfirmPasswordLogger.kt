package com.dashlane.createaccount.pages.confirmpassword

import androidx.annotation.StringDef



interface CreateAccountConfirmPasswordLogger {

    

    var origin: String?

    

    fun logLand(gdprApprovalRequired: Boolean)

    

    fun logBack()

    

    fun logPasswordVisibilityToggle(passwordShown: Boolean)

    

    fun logPasswordError()

    

    fun logNetworkError(@NetworkStep step: String)

    

    fun logCreateAccountSuccess()

    companion object {
        const val NW_ERR_CREATE_ACCOUNT = "Create"
    }

    @StringDef(
        NW_ERR_CREATE_ACCOUNT
    )
    @Retention(AnnotationRetention.SOURCE)
    annotation class NetworkStep
}