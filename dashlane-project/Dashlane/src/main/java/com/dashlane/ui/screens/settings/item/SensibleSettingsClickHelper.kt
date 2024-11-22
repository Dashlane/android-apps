package com.dashlane.ui.screens.settings.item

import android.content.Context
import com.dashlane.lock.LockEvent
import com.dashlane.lock.LockManager
import com.dashlane.lock.LockPrompt
import com.dashlane.utils.coroutines.inject.qualifiers.ApplicationCoroutineScope
import com.dashlane.utils.coroutines.inject.qualifiers.MainCoroutineDispatcher
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class SensibleSettingsClickHelper @Inject constructor(
    @ApplicationCoroutineScope
    private val applicationCoroutineScope: CoroutineScope,
    @MainCoroutineDispatcher
    private val mainCoroutineDispatcher: CoroutineDispatcher,
    private val lockManager: LockManager
) {
    fun perform(
        context: Context,
        origin: LockEvent.Unlock.Reason.WithCode.Origin = LockEvent.Unlock.Reason.WithCode.Origin.EDIT_SETTINGS,
        masterPasswordRecommended: Boolean = true,
        forceMasterPassword: Boolean = false,
        onUnlock: () -> Unit
    ) {
        if (forceMasterPassword || isMasterPasswordNecessary(masterPasswordRecommended)) {
            performActionAfterUnlock(context, origin, onUnlock)
        } else {
            onUnlock.invoke()
        }
    }

    private fun isMasterPasswordNecessary(masterPasswordRecommended: Boolean) =
        (masterPasswordRecommended && !lockManager.hasEnteredMP)

    private fun performActionAfterUnlock(
        context: Context,
        origin: LockEvent.Unlock.Reason.WithCode.Origin,
        onUnlock: () -> Unit
    ) {
        applicationCoroutineScope.launch(mainCoroutineDispatcher) {
            lockManager.showAndWaitLockActivityForReason(
                context = context,
                reason = LockEvent.Unlock.Reason.WithCode(UNLOCK_EVENT_CODE, origin),
                lockPrompt = LockPrompt.ForSettings,
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
        private const val UNLOCK_EVENT_CODE = 471
    }
}
