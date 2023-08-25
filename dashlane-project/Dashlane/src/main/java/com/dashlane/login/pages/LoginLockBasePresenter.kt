package com.dashlane.login.pages

import android.widget.Toast
import com.dashlane.R
import com.dashlane.dagger.singleton.SingletonProvider
import com.dashlane.login.lock.LockManager
import com.dashlane.login.root.LoginPresenter
import com.dashlane.ui.util.FinishingActivity
import com.dashlane.useractivity.log.usage.UsageLogConstant
import kotlinx.coroutines.CoroutineScope

abstract class LoginLockBasePresenter<P : LoginLockBaseContract.DataProvider, Q : LoginLockBaseContract.ViewProxy>(
    rootPresenter: LoginPresenter,
    coroutineScope: CoroutineScope,
    val lockManager: LockManager
) : LoginBasePresenter<P, Q>(rootPresenter, coroutineScope), LoginLockBaseContract.Presenter {

    override fun logoutTooManyAttempts(errorMessage: CharSequence?, logSent: Boolean) {
        val toaster = SingletonProvider.getToaster()
        if (!logSent) {
            providerOrNull?.logUsageLogCode75(UsageLogConstant.LockAction.logout)
        }
        if (errorMessage != null) {
            toaster.show(errorMessage, Toast.LENGTH_LONG)
        } else {
            val toastTextResId = when (lockTypeName) {
                UsageLogConstant.LockType.fingerPrint -> R.string.lock_fingerprint_force_logout_fingerprint_incorrect_too_much
                UsageLogConstant.LockType.pin -> R.string.lock_pincode_force_logout_pin_missed_too_much
                UsageLogConstant.LockType.master -> R.string.lock_force_logout_password_incorrect_too_much
                else -> null
            }
            if (toastTextResId != null) {
                toaster.show(toastTextResId, Toast.LENGTH_LONG)
            }
        }

        rootPresenter.onPrimaryFactorTooManyAttempts()
    }

    override fun getAttemptsFailed(): Int {
        return lockManager.getFailUnlockAttemptCount()
    }

    override fun onBackPressed(): Boolean {
        val handled = super.onBackPressed()
        if (provider.lockSetting.isLoggedIn && !provider.lockSetting.isLockCancelable) {
            FinishingActivity.finishApplication(activity!!)
            return true
        }
        return handled
    }
}
