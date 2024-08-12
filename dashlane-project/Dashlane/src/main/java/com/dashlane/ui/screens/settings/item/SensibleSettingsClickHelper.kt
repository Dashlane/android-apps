package com.dashlane.ui.screens.settings.item

import android.content.Context
import com.dashlane.R
import com.dashlane.lock.LockHelper
import com.dashlane.lock.UnlockEvent
import com.dashlane.login.lock.LockManager
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
    private val lockManager: LockManager
) {

    private var requireMasterPassword = true

    fun perform(
        context: Context,
        origin: UnlockEvent.Reason.WithCode.Origin = UnlockEvent.Reason.WithCode.Origin.EDIT_SETTINGS,
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
        (masterPasswordRecommended && requireMasterPassword && !lockManager.hasEnteredMP)

    private fun performActionAfterUnlock(
        context: Context,
        origin: UnlockEvent.Reason.WithCode.Origin,
        onUnlock: () -> Unit
    ) {
        applicationCoroutineScope.launch(mainCoroutineDispatcher) {
            lockManager.showAndWaitLockActivityForReason(
                context,
                UnlockEvent.Reason.WithCode(UNLOCK_EVENT_CODE, origin),
                LockHelper.PROMPT_LOCK_FOR_SETTINGS,
                context.getString(R.string.please_enter_master_password_to_edit_settings)
            )?.takeIf {
                val reason = it.reason
                it.isSuccess() && reason is UnlockEvent.Reason.WithCode && reason.requestCode == UNLOCK_EVENT_CODE
            }?.let {
                requireMasterPassword = false
                onUnlock.invoke()
            }
        }
    }

    companion object {
        private const val UNLOCK_EVENT_CODE = 471
    }
}
