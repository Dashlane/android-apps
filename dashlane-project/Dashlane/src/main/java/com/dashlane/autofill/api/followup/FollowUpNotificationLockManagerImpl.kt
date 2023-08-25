package com.dashlane.autofill.api.followup

import android.content.Context
import com.dashlane.followupnotification.api.FollowUpNotificationLockManager
import com.dashlane.login.lock.LockManager
import com.dashlane.util.inject.qualifiers.ApplicationCoroutineScope
import com.dashlane.util.inject.qualifiers.MainCoroutineDispatcher
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class FollowUpNotificationLockManagerImpl @Inject constructor(
    @ApplicationContext
    private val context: Context,
    @ApplicationCoroutineScope
    private val applicationCoroutineScope: CoroutineScope,
    @MainCoroutineDispatcher
    private val mainCoroutineDispatcher: CoroutineDispatcher,
    val lockManager: LockManager
) : FollowUpNotificationLockManager {
    override fun isAccountLocked() = lockManager.isLocked

    override fun askForUnlockAndExecute(
        onUnlockSuccessful: () -> Unit
    ) {
        if (lockManager.isLocked) {
            applicationCoroutineScope.launch(mainCoroutineDispatcher) {
                val lock = lockManager.showLockActivityForFollowUpNotification(context)
                if (lock?.isSuccess() == true) {
                    onUnlockSuccessful()
                }
            }
        } else {
            onUnlockSuccessful()
        }
    }
}
