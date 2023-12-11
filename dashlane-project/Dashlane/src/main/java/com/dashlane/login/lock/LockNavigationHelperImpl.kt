package com.dashlane.login.lock

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.dashlane.lock.LockHelper
import com.dashlane.lock.LockNavigationHelper
import com.dashlane.lock.LockWatcher
import com.dashlane.lock.UnlockEvent
import com.dashlane.lock.UnlockEventCoroutineListener
import com.dashlane.login.LoginActivity
import com.dashlane.navigation.NavigationConstants
import com.dashlane.security.DashlaneIntent
import com.dashlane.util.tryOrNull
import com.dashlane.vault.summary.SummaryObject
import kotlinx.coroutines.withTimeout
import javax.inject.Inject

class LockNavigationHelperImpl @Inject constructor(
    private val messageHelper: LockMessageHelper,
    private val lockTypeManager: LockTypeManager,
    private val lockWatcher: LockWatcher
) :
    LockNavigationHelper {

    override suspend fun showAndWaitLockActivityForReason(
        context: Context,
        reason: UnlockEvent.Reason,
        lockPrompt: Int,
        customMessage: String?,
        timeoutMs: Long
    ): UnlockEvent? {
        val lockWatcher = UnlockEventCoroutineListener(lockWatcher)
        showLockActivityForReason(
            context,
            reason,
            lockPrompt,
            customMessage
        )
        return tryOrNull { withTimeout(timeoutMs) { lockWatcher.await() } }
    }

    override fun showLockActivityForReason(
        context: Context,
        reason: UnlockEvent.Reason,
        @LockHelper.LockPrompt lockPrompt: Int,
        customMessage: String?
    ) {
        context.startActivity(getIntentLock(context, reason, lockPrompt, customMessage))
    }

    override fun showLockForBiometricRecovery(
        activity: Activity,
        requestCode: Int,
        customMessage: String?,
        subTitle: String?
    ) {
        val intent =
            getIntentLock(
                activity,
                UnlockEvent.Reason.WithCode(requestCode),
                LockHelper.PROMPT_LOCK_REGULAR,
                customMessage,
                subTitle,
                false
            )
        activity.startActivityForResult(intent, requestCode)
    }

    override suspend fun showAndWaitLockActivityForItem(context: Context, item: SummaryObject): UnlockEvent? {
        val message = messageHelper.getMessageUnlockForItem(context, item)
        val itemType = item.syncObjectType
        val itemUid = item.id

        val reason = UnlockEvent.Reason.OpenItem(itemType.xmlObjectName, itemUid)
        return showAndWaitLockActivityForReason(context, reason, LockHelper.PROMPT_LOCK_FOR_ITEM, message)
    }

    override fun showLockActivityToSetPinCode(context: Context) {
        val lockIntent = Intent(context, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(LockSetting.EXTRA_IS_LOCK_CANCELABLE, true)
            putExtra(LockSetting.EXTRA_LOCK_TYPE_IS_PIN_SET, true)
        }
        context.startActivity(lockIntent)
    }

    override fun showLockActivity(context: Context) {
        val lockIntent = Intent(context, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(LockSetting.EXTRA_IS_LOCK_CANCELABLE, false)
            putExtra(LockSetting.EXTRA_LOCK_TYPE, lockTypeManager.getLockType())
            putExtra(LockSetting.EXTRA_LOCK_REASON, UnlockEvent.Reason.AppAccess())
        }
        context.startActivity(lockIntent)
    }

    override fun showLockActivityForAutofillApi(context: Context) {
        val reason = UnlockEvent.Reason.AccessFromAutofillApi()
        val lockIntent = getIntentLock(context, reason, LockHelper.PROMPT_LOCK_REGULAR, null).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK or
                    Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS or
                    Intent.FLAG_ACTIVITY_NO_HISTORY
            putExtra(NavigationConstants.LOGIN_CALLED_FROM_INAPP_LOGIN, true)
            putExtra(LockSetting.EXTRA_AS_DIALOG, true)
        }
        context.startActivity(lockIntent)
    }

    override suspend fun showLockActivityForFollowUpNotification(context: Context): UnlockEvent? {
        val lockWatcher = UnlockEventCoroutineListener(lockWatcher)

        val reason = UnlockEvent.Reason.AccessFromFollowUpNotification()
        val lockIntent = getIntentLock(context, reason, LockHelper.PROMPT_LOCK_REGULAR, null).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK or
                    Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS or
                    Intent.FLAG_ACTIVITY_NO_HISTORY
            putExtra(NavigationConstants.LOGIN_CALLED_FROM_INAPP_LOGIN, true)
            putExtra(LockSetting.EXTRA_AS_DIALOG, true)
        }
        context.startActivity(lockIntent)
        return tryOrNull { withTimeout(30_000) { lockWatcher.await() } }
    }

    override fun showLockActivityForInAppLogin(context: Context, itemUID: String?) {
        val reason = UnlockEvent.Reason.AccessFromExternalComponent(itemUID)
        val lockIntent = getIntentLock(context, reason, LockHelper.PROMPT_LOCK_REGULAR, null).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK or
                    Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS or
                    Intent.FLAG_ACTIVITY_NO_HISTORY
            putExtra(NavigationConstants.LOGIN_CALLED_FROM_INAPP_LOGIN, true)
        }
        context.startActivity(lockIntent)
    }

    override fun logoutAndCallLoginScreenForInAppLogin(context: Context) {
        val loginActivityIntent = DashlaneIntent.newInstance(context, LoginActivity::class.java).apply {
            putExtra(NavigationConstants.LOGIN_CALLED_FROM_INAPP_LOGIN, true)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        context.startActivity(loginActivityIntent)
    }

    private fun getIntentLock(
        context: Context,
        reason: UnlockEvent.Reason,
        @LockHelper.LockPrompt lockPrompt: Int,
        customMessage: String?,
        subTitle: String? = null,
        clearTop: Boolean = true
    ): Intent = Intent(context, LoginActivity::class.java).apply {
        if (clearTop) {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        putExtra(LockSetting.EXTRA_LOCK_REASON, reason)
        @LockTypeManager.LockType val lockType = lockTypeManager.getLockType(lockPrompt)
        putExtra(LockSetting.EXTRA_LOCK_TYPE, lockType)
        if (subTitle != null) {
            putExtra(LockSetting.EXTRA_SUB_TOPIC_LOCK, subTitle)
        }
        if (customMessage != null) {
            putExtra(LockSetting.EXTRA_TOPIC_LOCK, customMessage)
        }
        if (reason is UnlockEvent.Reason.AccessFromExternalComponent ||
            reason is UnlockEvent.Reason.OpenItem ||
            reason is UnlockEvent.Reason.WithCode
        ) {
            putExtra(LockSetting.EXTRA_AS_DIALOG, true)
        }
    }
}