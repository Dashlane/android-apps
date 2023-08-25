package com.dashlane.login.pages.token

import com.dashlane.authentication.RegisteredUserDevice
import com.dashlane.login.pages.LoginBaseContract

interface LoginTokenContract {

    interface ViewProxy : LoginBaseContract.View {

        var tokenText: String

        var tokenViewWidth: Int

        fun initDebug(username: String)
    }

    interface Presenter : LoginBaseContract.Presenter {

        fun onCodeCompleted()

        fun onWhereIsClicked()
    }

    interface DataProvider : LoginBaseContract.DataProvider {
        suspend fun validateToken(token: String, auto: Boolean): Pair<RegisteredUserDevice, String?>

        suspend fun resendToken()
    }

    class InvalidTokenException(val lockedOut: Boolean, cause: Throwable? = null) : Exception(cause)
}