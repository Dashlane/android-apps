package com.dashlane.ui.screens.settings.item

import android.content.Context
import com.dashlane.lock.LockEvent
import com.dashlane.lock.LockManager
import com.dashlane.lock.LockPrompt
import com.dashlane.utils.coroutines.inject.qualifiers.ApplicationCoroutineScope
import com.dashlane.utils.coroutines.inject.qualifiers.MainCoroutineDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class SensibleSettingsClickHelper @Inject constructor(
    @ApplicationCoroutineScope
    private val applicationCoroutineScope: CoroutineScope,
    @MainCoroutineDispatcher
    private val mainCoroutineDispatcher: CoroutineDispatcher,
    private val lockManager: LockManager,
) {
    fun perform(
        context: Context,
        onUnlock: () -> Unit,
    ) {
        performActionAfterUnlock(
            context = context,
            origin = LockEvent.Unlock.Reason.WithCode.Origin.EDIT_SETTINGS,
            forceMasterPassword = false,
            onUnlock = onUnlock,
        )
    }

    fun perform(
        context: Context,
        origin: LockEvent.Unlock.Reason.WithCode.Origin = LockEvent.Unlock.Reason.WithCode.Origin.EDIT_SETTINGS,
        masterPasswordRecommended: Boolean = true,
        forceMasterPassword: Boolean,
        onUnlock: () -> Unit,
    ) {
        if (forceMasterPassword || isMasterPasswordNecessary(masterPasswordRecommended)) {
            performActionAfterUnlock(context, origin, forceMasterPassword, onUnlock)
        } else {
            onUnlock.invoke()
        }
    }

    private fun isMasterPasswordNecessary(masterPasswordRecommended: Boolean) =
        (masterPasswordRecommended && !lockManager.hasEnteredMP)

    private fun performActionAfterUnlock(
        context: Context,
        origin: LockEvent.Unlock.Reason.WithCode.Origin,
        forceMasterPassword: Boolean,
        onUnlock: () -> Unit,
    ) {
        applicationCoroutineScope.launch(mainCoroutineDispatcher) {
            lockManager.showAndWaitLockActivityForReason(
                context = context,
                reason = LockEvent.Unlock.Reason.WithCode(UNLOCK_EVENT_CODE, origin),
                lockPrompt = LockPrompt.ForSettings(forceMasterPassword = forceMasterPassword),
            ).takeIf { lockEvent ->
                lockEvent is LockEvent.Unlock &&
                    lockEvent.reason is LockEvent.Unlock.Reason.WithCode &&
                    (lockEvent.reason as LockEvent.Unlock.Reason.WithCode).requestCode == UNLOCK_EVENT_CODE
            }?.let {
                onUnlock.invoke()
            }
        }
    }

    companion object {
        const val UNLOCK_EVENT_CODE = 471
    }
}