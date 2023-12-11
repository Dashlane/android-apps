package com.dashlane.login.pages.password

import android.content.Intent
import com.dashlane.accountrecoverykey.AccountRecoveryStatus
import com.dashlane.login.pages.LoginLockBaseContract

interface LoginPasswordContract {

    interface ViewProxy : LoginLockBaseContract.ViewProxy {

        val passwordText: CharSequence

        fun showPasswordHelp()

        fun showRecoveryDialog()

        fun setUnlockTopic(topic: String?)

        fun showSwitchAccount(loginHistory: List<String>)

        fun hideSwitchAccount()

        fun showUnlockLayout(useUnlockLayout: Boolean)

        fun setCancelBtnText(string: String?)

        fun setExplanation(string: String)

        var isDialog: Boolean
    }

    interface Presenter : LoginLockBaseContract.Presenter {

        fun onClickLoginHelpRequested()

        fun onClickForgotPassword()

        fun onClickChangeAccount(email: String?)

        fun onCancelClicked()

        fun onClickForgotButton()

        fun onClickBiometricRecovery()

        fun onClickAccountRecoveryKey()
    }

    interface DataProvider : LoginLockBaseContract.DataProvider {

        val loginHistory: List<String>

        suspend fun validatePassword(password: CharSequence, leaveAfterSuccess: Boolean): SuccessfulLogin

        suspend fun getAccountRecoveryKeyStatus(): AccountRecoveryStatus

        fun loginHelp(): Intent

        fun passwordForgotten(): Intent

        suspend fun changeAccount(email: String?): Intent

        fun askMasterPasswordLater()

        val canMakeBiometricRecovery: Boolean

        suspend fun loadStaleSession()

        fun unloadSession()

        fun getChangeMPIntent(): Intent?

        fun getAccountRecoveryKeyIntent(): Intent?

        fun onPromptBiometricForRecovery()
    }

    class SuccessfulLogin(val intent: Intent?)

    class InvalidPasswordException(
        val reason: InvalidReason = InvalidReason.INVALID,
        cause: Throwable? = null
    ) : Exception(cause) {
        enum class InvalidReason {
            INVALID,
            FAILED_UNLOCK,
            EMPTY
        }
    }

    class AccountResetException(cause: Throwable? = null) : Exception(cause)
}