package com.dashlane.lock

import android.app.Activity
import android.content.Context
import com.dashlane.vault.summary.SummaryObject
import java.util.concurrent.TimeUnit

interface LockNavigationHelper {
    fun showLockActivityForReason(
        context: Context,
        reason: UnlockEvent.Reason,
        @LockHelper.LockPrompt lockPrompt: Int,
        customMessage: String?
    )

    fun showLockForAccountReset(
        activity: Activity,
        requestCode: Int,
        customMessage: String?,
        subTitle: String?
    )

    suspend fun showAndWaitLockActivityForReason(
        context: Context,
        reason: UnlockEvent.Reason,
        @LockHelper.LockPrompt lockPrompt: Int,
        customMessage: String?,
        timeoutMs: Long = TimeUnit.MINUTES.toMillis(1)
    ): UnlockEvent?

    fun showLockActivityForAutofillApi(context: Context)
    suspend fun showLockActivityForFollowUpNotification(context: Context): UnlockEvent?
    fun showLockActivityForInAppLogin(context: Context, itemUID: String?)
    fun showLockActivityToSetPinCode(context: Context, isReset: Boolean)
    fun showLockActivity(context: Context)

    fun logoutAndCallLoginScreenForInAppLogin(context: Context)

    

    suspend fun showAndWaitLockActivityForItem(context: Context, item: SummaryObject): UnlockEvent?
}