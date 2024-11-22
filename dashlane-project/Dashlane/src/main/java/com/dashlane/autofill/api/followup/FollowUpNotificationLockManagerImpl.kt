package com.dashlane.autofill.api.followup

import android.content.Context
import com.dashlane.followupnotification.api.FollowUpNotificationLockManager
import com.dashlane.lock.LockEvent
import com.dashlane.lock.LockManager
import com.dashlane.utils.coroutines.inject.qualifiers.ApplicationCoroutineScope
import com.dashlane.utils.coroutines.inject.qualifiers.MainCoroutineDispatcher
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

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
                val lockEvent = lockManager.showLockActivityForFollowUpNotification(context)
                if (lockEvent is LockEvent.Unlock) {
                    onUnlockSuccessful()
                }
            }
        } else {
            onUnlockSuccessful()
        }
    }
}
