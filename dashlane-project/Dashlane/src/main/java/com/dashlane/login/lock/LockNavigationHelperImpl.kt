package com.dashlane.login.lock

import android.content.Context
import android.content.Intent
import com.dashlane.lock.LockEvent
import com.dashlane.lock.LockNavigationHelper
import com.dashlane.lock.LockPrompt
import com.dashlane.lock.LockSetting
import com.dashlane.lock.LockWatcher
import com.dashlane.login.LoginActivity
import com.dashlane.navigation.NavigationConstants
import com.dashlane.security.DashlaneIntent
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.timeout

class LockNavigationHelperImpl @Inject constructor(
    private val lockWatcher: LockWatcher
) :
    LockNavigationHelper {

    override suspend fun showAndWaitLockActivityForReason(
        context: Context,
        reason: LockEvent.Unlock.Reason,
        lockPrompt: LockPrompt,
        timeoutMs: Long
    ): LockEvent {
        showLockActivityForReason(
            context = context,
            reason = reason,
            lockPrompt = lockPrompt,
        )
        return lockWatcher.lockEventFlow
            .timeout(timeoutMs.milliseconds)
            .catch { emit(LockEvent.Cancelled) }
            .first()
    }

    override fun showLockActivityForReason(
        context: Context,
        reason: LockEvent.Unlock.Reason,
        lockPrompt: LockPrompt,
    ) {
        context.startActivity(getIntentLock(context, reason, lockPrompt))
    }

    override fun showLockActivity(context: Context) {
        val lockIntent = Intent(context, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(LockSetting.EXTRA_IS_LOCK_CANCELABLE, false)
            putExtra(LockSetting.EXTRA_LOCK_REASON, LockEvent.Unlock.Reason.AppAccess)
        }
        context.startActivity(lockIntent)
    }

    override fun showLockActivityForAutofillApi(context: Context) {
        val reason = LockEvent.Unlock.Reason.AccessFromAutofillApi
        val lockIntent = getIntentLock(context, reason, LockPrompt.Regular).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TASK or
                Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS or
                Intent.FLAG_ACTIVITY_NO_HISTORY
            putExtra(NavigationConstants.LOGIN_CALLED_FROM_INAPP_LOGIN, true)
            putExtra(LockSetting.EXTRA_AS_DIALOG, true)
        }
        context.startActivity(lockIntent)
    }

    override suspend fun showLockActivityForFollowUpNotification(context: Context): LockEvent {
        val reason = LockEvent.Unlock.Reason.AccessFromFollowUpNotification
        val lockIntent = getIntentLock(context, reason, LockPrompt.Regular).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TASK or
                Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS or
                Intent.FLAG_ACTIVITY_NO_HISTORY
            putExtra(NavigationConstants.LOGIN_CALLED_FROM_INAPP_LOGIN, true)
            putExtra(LockSetting.EXTRA_AS_DIALOG, true)
        }
        context.startActivity(lockIntent)

        return lockWatcher.lockEventFlow
            .timeout(30.seconds)
            .catch { emit(LockEvent.Cancelled) }
            .first()
    }

    override fun showLockActivityForInAppLogin(context: Context, itemUID: String?) {
        val reason = LockEvent.Unlock.Reason.AccessFromExternalComponent(itemUID)
        val lockIntent = getIntentLock(context, reason, LockPrompt.Regular).apply {
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
        reason: LockEvent.Unlock.Reason,
        lockPrompt: LockPrompt,
        clearTop: Boolean = true
    ): Intent = Intent(context, LoginActivity::class.java).apply {
        if (clearTop) {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        putExtra(LockSetting.EXTRA_LOCK_REASON, reason)
        putExtra(LockSetting.EXTRA_LOCK_PROMPT, lockPrompt)

        when (reason) {
            is LockEvent.Unlock.Reason.AccessFromExternalComponent,
            is LockEvent.Unlock.Reason.OpenItem -> putExtra(LockSetting.EXTRA_AS_DIALOG, true)
            is LockEvent.Unlock.Reason.WithCode -> {
                when (reason.origin) {
                    null,
                    LockEvent.Unlock.Reason.WithCode.Origin.EDIT_SETTINGS -> putExtra(LockSetting.EXTRA_AS_DIALOG, true)
                    LockEvent.Unlock.Reason.WithCode.Origin.CHANGE_MASTER_PASSWORD -> putExtra(LockSetting.EXTRA_IS_MASTER_PASSWORD_RESET, true)
                }
            }
            else -> Unit
        }
    }
}