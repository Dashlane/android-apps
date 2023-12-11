package com.dashlane.login.pages

import android.widget.Toast
import com.dashlane.R
import com.dashlane.login.lock.LockManager
import com.dashlane.login.lock.LockTypeManager
import com.dashlane.login.root.LoginPresenter
import com.dashlane.ui.util.FinishingActivity
import com.dashlane.util.Toaster
import kotlinx.coroutines.CoroutineScope

abstract class LoginLockBasePresenter<P : LoginLockBaseContract.DataProvider, Q : LoginLockBaseContract.ViewProxy>(
    val lockManager: LockManager,
    val toaster: Toaster,
    rootPresenter: LoginPresenter,
    coroutineScope: CoroutineScope
) : LoginBasePresenter<P, Q>(rootPresenter, coroutineScope), LoginLockBaseContract.Presenter {

    override fun logoutTooManyAttempts(errorMessage: CharSequence?, showToast: Boolean) {
        if (showToast) {
            if (errorMessage != null) {
                toaster.show(errorMessage, Toast.LENGTH_LONG)
            } else {
                val toastTextResId = when (lockTypeName) {
                    LockTypeManager.LOCK_TYPE_BIOMETRIC -> R.string.lock_fingerprint_force_logout_fingerprint_incorrect_too_much
                    LockTypeManager.LOCK_TYPE_PIN_CODE -> R.string.lock_pincode_force_logout_pin_missed_too_much
                    LockTypeManager.LOCK_TYPE_MASTER_PASSWORD -> R.string.lock_force_logout_password_incorrect_too_much
                    else -> null
                }
                if (toastTextResId != null) {
                    toaster.show(toastTextResId, Toast.LENGTH_LONG)
                }
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
