package com.dashlane.wear.lock

import android.content.Context
import com.dashlane.lock.LockEvent
import com.dashlane.lock.LockNavigationHelper
import com.dashlane.lock.LockPrompt
import javax.inject.Inject

class WearLockNavigationHelper @Inject constructor() : LockNavigationHelper {
    override fun showLockActivityForReason(
        context: Context,
        reason: LockEvent.Unlock.Reason,
        lockPrompt: LockPrompt,
    ) {
        throw UnsupportedOperationException()
    }

    override suspend fun showAndWaitLockActivityForReason(
        context: Context,
        reason: LockEvent.Unlock.Reason,
        lockPrompt: LockPrompt,
        timeoutMs: Long,
    ): LockEvent {
        
        throw UnsupportedOperationException()
    }

    override fun showLockActivityForAutofillApi(context: Context) {
        throw UnsupportedOperationException()
    }

    override suspend fun showLockActivityForFollowUpNotification(context: Context): LockEvent {
        throw UnsupportedOperationException()
    }

    override fun showLockActivityForInAppLogin(context: Context, itemUID: String?) {
        throw UnsupportedOperationException()
    }

    override fun showLockActivity(context: Context) {
        throw UnsupportedOperationException()
    }

    override fun logoutAndCallLoginScreenForInAppLogin(context: Context) {
        throw UnsupportedOperationException()
    }
}