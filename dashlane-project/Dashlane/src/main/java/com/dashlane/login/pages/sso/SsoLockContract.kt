package com.dashlane.login.pages.sso

import android.content.Intent
import com.dashlane.authentication.login.SsoInfo
import com.dashlane.authentication.sso.utils.UserSsoInfo
import com.dashlane.login.pages.LoginLockBaseContract

interface SsoLockContract {
    interface ViewProxy : LoginLockBaseContract.ViewProxy {
        fun setCancelable(cancelable: Boolean)
        fun canSwitchAccount(switchAccount: Boolean)
        fun initSpinner(loginHistory: List<String>)
    }

    interface Presenter : LoginLockBaseContract.Presenter {
        fun onClickChangeAccount(email: String?)
        fun onCancelClicked()
    }

    interface DataProvider : LoginLockBaseContract.DataProvider {
        val loginHistory: List<String>
        suspend fun getSsoInfo(): SsoInfo
        suspend fun unlock(userSsoInfo: UserSsoInfo)
        suspend fun changeAccount(email: String? = null): Intent
    }

    class NoSessionLoadedException : Exception()
}