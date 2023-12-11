package com.dashlane.login.pages

import android.content.Intent
import com.dashlane.login.lock.LockSetting
import com.dashlane.login.lock.LockTypeManager

interface LoginLockBaseContract {

    interface ViewProxy : LoginBaseContract.View

    interface Presenter : LoginBaseContract.Presenter {
        @LockTypeManager.LockType
        val lockTypeName: Int
        fun getAttemptsFailed(): Int
        fun logoutTooManyAttempts(errorMessage: CharSequence? = null, showToast: Boolean = true)
    }

    interface DataProvider : LoginBaseContract.DataProvider {

        val lockSetting: LockSetting
        fun initLockSetting(lockSetting: LockSetting)
        fun onUnlockSuccess(): Intent?
        fun createNextActivityIntent(): Intent?
    }
}