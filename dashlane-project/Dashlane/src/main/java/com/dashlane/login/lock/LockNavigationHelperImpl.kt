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
import com.dashlane.util.DevUtil
import com.dashlane.util.tryOrNull
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.util.desktopId
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
        val intent = getIntentLock(context, reason, lockPrompt, customMessage)
        DevUtil.startActivityOrDefaultErrorMessage(context, intent)
    }

    override fun showLockForAccountReset(
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
        val itemType = item.syncObjectType.desktopId
        val itemUid = item.id

        val reason = UnlockEvent.Reason.OpenItem(itemType, itemUid)
        return showAndWaitLockActivityForReason(context, reason, LockHelper.PROMPT_LOCK_FOR_ITEM, message)
    }

    override fun showLockActivityToSetPinCode(context: Context, isReset: Boolean) {
        val lock = Intent(context, LoginActivity::class.java)
        lock.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        lock.putExtra(LockSetting.EXTRA_IS_LOCK_CANCELABLE, true)
        lock.putExtra(LockSetting.EXTRA_LOCK_TYPE_IS_PIN_SET, true)
        lock.putExtra(LockSetting.EXTRA_LOCK_TYPE_IS_PIN_RESET, isReset)
        DevUtil.startActivityOrDefaultErrorMessage(context, lock)
    }

    override fun showLockActivity(context: Context) {
        val lock = Intent(context, LoginActivity::class.java)
        lock.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        lock.putExtra(LockSetting.EXTRA_IS_LOCK_CANCELABLE, false)
        lock.putExtra(LockSetting.EXTRA_LOCK_TYPE, lockTypeManager.getLockType())
        lock.putExtra(LockSetting.EXTRA_LOCK_REASON, UnlockEvent.Reason.AppAccess())
        DevUtil.startActivityOrDefaultErrorMessage(context, lock)
    }

    override fun showLockActivityForAutofillApi(context: Context) {
        val reason = UnlockEvent.Reason.AccessFromAutofillApi()
        val intent = getIntentLock(context, reason, LockHelper.PROMPT_LOCK_REGULAR, null)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent
            .FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS or Intent.FLAG_ACTIVITY_NO_HISTORY
        intent.putExtra(NavigationConstants.LOGIN_CALLED_FROM_INAPP_LOGIN, true)
        intent.putExtra(LockSetting.EXTRA_AS_DIALOG, true)
        DevUtil.startActivityOrDefaultErrorMessage(context, intent, false)
    }

    override suspend fun showLockActivityForFollowUpNotification(context: Context): UnlockEvent? {
        val lockWatcher = UnlockEventCoroutineListener(lockWatcher)

        val reason = UnlockEvent.Reason.AccessFromFollowUpNotification()
        val intent = getIntentLock(context, reason, LockHelper.PROMPT_LOCK_REGULAR, null)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent
            .FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS or Intent.FLAG_ACTIVITY_NO_HISTORY
        intent.putExtra(NavigationConstants.LOGIN_CALLED_FROM_INAPP_LOGIN, true)
        intent.putExtra(LockSetting.EXTRA_AS_DIALOG, true)
        DevUtil.startActivityOrDefaultErrorMessage(context, intent, false)
        return tryOrNull { withTimeout(30_000) { lockWatcher.await() } }
    }

    override fun showLockActivityForInAppLogin(context: Context, itemUID: String?) {
        val reason = UnlockEvent.Reason.AccessFromExternalComponent(itemUID)
        val intent = getIntentLock(context, reason, LockHelper.PROMPT_LOCK_REGULAR, null)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent
            .FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS or Intent.FLAG_ACTIVITY_NO_HISTORY
        intent.putExtra(NavigationConstants.LOGIN_CALLED_FROM_INAPP_LOGIN, true)
        DevUtil.startActivityOrDefaultErrorMessage(context, intent, false)
    }

    override fun logoutAndCallLoginScreenForInAppLogin(context: Context) {
        val loginActivityIntent = DashlaneIntent.newInstance(context, LoginActivity::class.java)
        loginActivityIntent.putExtra(NavigationConstants.LOGIN_CALLED_FROM_INAPP_LOGIN, true)
        loginActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        loginActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        DevUtil.startActivityOrDefaultErrorMessage(context, loginActivityIntent, false)
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