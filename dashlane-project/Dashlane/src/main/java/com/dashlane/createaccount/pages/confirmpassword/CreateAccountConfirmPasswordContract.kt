package com.dashlane.createaccount.pages.confirmpassword

import androidx.annotation.StringRes
import com.dashlane.createaccount.pages.CreateAccountBaseContract
import com.dashlane.cryptography.ObfuscatedByteArray
import com.skocken.presentation.definition.Base

interface CreateAccountConfirmPasswordContract {

    interface ViewProxy : Base.IView {

        val passwordText: CharSequence

        fun showError(@StringRes errorResId: Int)
        fun setRecapPassword(password: String)
    }

    interface Presenter : CreateAccountBaseContract.Presenter

    interface DataProvider : CreateAccountBaseContract.DataProvider {

        val biometricAvailable: Boolean

        val requiresTosApproval: Boolean

        val clearPassword: String

        suspend fun validatePassword(password: CharSequence): PasswordSuccess
    }

    data class PasswordSuccess(val username: String, val password: ObfuscatedByteArray)

    class PasswordMismatchException : Exception()
}