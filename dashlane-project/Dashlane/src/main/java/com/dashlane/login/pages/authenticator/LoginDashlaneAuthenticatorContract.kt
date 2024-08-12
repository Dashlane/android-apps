package com.dashlane.login.pages.authenticator

import com.dashlane.authentication.AuthenticationSecondFactor
import com.dashlane.authentication.RegisteredUserDevice
import com.dashlane.login.pages.LoginBaseContract

interface LoginDashlaneAuthenticatorContract {

    interface ViewProxy : LoginBaseContract.View {
        var showTotpAvailable: Boolean
        fun showLoading()
        fun showSuccess(onAnimEnd: () -> Unit)
    }

    interface Presenter : LoginBaseContract.Presenter {
        fun onUseAlternativeClicked()
        fun onResendRequestClicked()
    }

    interface DataProvider : LoginBaseContract.DataProvider {
        val secondFactor: AuthenticationSecondFactor

        suspend fun executeAuthenticatorAuthentication(): Pair<RegisteredUserDevice, String?>

        suspend fun sendEmailToken(emailToken: AuthenticationSecondFactor.EmailToken)
    }
}