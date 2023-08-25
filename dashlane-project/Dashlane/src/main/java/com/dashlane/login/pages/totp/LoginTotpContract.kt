package com.dashlane.login.pages.totp

import com.dashlane.authentication.AuthenticationSecondFactor
import com.dashlane.authentication.RegisteredUserDevice
import com.dashlane.login.pages.LoginBaseContract
import kotlinx.coroutines.CoroutineScope

interface LoginTotpContract {

    interface ViewProxy : LoginBaseContract.View {
        var showAuthenticatorAvailable: Boolean

        var showDuoAvailable: Boolean

        var showU2fAvailable: Boolean

        val totpText: String

        var tokenViewWidth: Int

        fun showU2fKeyDetected()

        fun showU2fKeyNeedsUserPresence()

        fun showU2fKeyMatched()
    }

    interface Presenter : LoginBaseContract.Presenter {

        fun onCodeCompleted()

        fun notifyUnknownError()

        fun notifyTotpError(lockedOut: Boolean)

        fun notifyU2fKeyDetected()

        fun notifyU2fKeyMatchFailError()

        fun notifyU2fKeyNeedsUserPresence()

        fun notifyU2fKeyMatched()

        fun onTotpSuccess(registeredUserDevice: RegisteredUserDevice, authTicket: String?)

        fun onDuoClicked()

        fun onAuthenticatorClicked()
    }

    interface DataProvider : LoginBaseContract.DataProvider {
        val secondFactor: AuthenticationSecondFactor.Totp

        suspend fun validateTotp(otp: String, auto: Boolean)

        suspend fun executeDuoAuthentication()

        suspend fun executeU2fAuthentication(coroutineScope: CoroutineScope)
    }

    class DuoTimeoutException(cause: Throwable? = null) : Exception(cause)
    class DuoDeniedException(cause: Throwable? = null) : Exception(cause)
}