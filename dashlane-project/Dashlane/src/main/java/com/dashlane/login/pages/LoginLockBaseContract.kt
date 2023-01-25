package com.dashlane.login.pages

import android.content.Intent
import com.dashlane.login.lock.LockSetting
import com.dashlane.useractivity.log.usage.UsageLog



interface LoginLockBaseContract {

    interface ViewProxy : LoginBaseContract.View

    interface Presenter : LoginBaseContract.Presenter {
        val lockTypeName: String
        fun getAttemptsFailed(): Int
        fun logoutTooManyAttempts(errorMessage: CharSequence? = null, logSent: Boolean = false)
    }

    interface DataProvider : LoginBaseContract.DataProvider {

        val lockSetting: LockSetting
        fun initLockSetting(lockSetting: LockSetting)
        fun onUnlockSuccess(): Intent?
        fun createNextActivityIntent(): Intent?
        fun log(log: UsageLog)
        fun logUsageLogCode75(action: String)
    }
}