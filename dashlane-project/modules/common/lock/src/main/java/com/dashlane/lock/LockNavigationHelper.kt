package com.dashlane.lock

import android.content.Context
import java.util.concurrent.TimeUnit

interface LockNavigationHelper {
    fun showLockActivityForReason(
        context: Context,
        reason: LockEvent.Unlock.Reason,
        lockPrompt: LockPrompt,
    )

    suspend fun showAndWaitLockActivityForReason(
        context: Context,
        reason: LockEvent.Unlock.Reason,
        lockPrompt: LockPrompt,
        timeoutMs: Long = TimeUnit.MINUTES.toMillis(1)
    ): LockEvent

    fun showLockActivityForAutofillApi(context: Context)
    suspend fun showLockActivityForFollowUpNotification(context: Context): LockEvent
    fun showLockActivityForInAppLogin(context: Context, itemUID: String?)
    fun showLockActivity(context: Context)

    fun logoutAndCallLoginScreenForInAppLogin(context: Context)
}