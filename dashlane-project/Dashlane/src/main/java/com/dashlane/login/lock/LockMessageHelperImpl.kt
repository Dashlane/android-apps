package com.dashlane.login.lock

import android.content.Context
import com.dashlane.R
import com.dashlane.lock.LockHelper
import com.dashlane.vault.summary.SummaryObject
import javax.inject.Inject

class LockMessageHelperImpl @Inject constructor(
    private val lockTypeManager: LockTypeManager
) : LockMessageHelper {
    override fun getMessageUnlockForItem(context: Context, item: SummaryObject): String {
        val lockType = lockTypeManager.getLockType(LockHelper.PROMPT_LOCK_FOR_ITEM)
        val isBiometricUnlock = lockType == LockTypeManager.LOCK_TYPE_BIOMETRIC
        val isSecureNote = item is SummaryObject.SecureNote

        val resId = when {
            isSecureNote && isBiometricUnlock -> R.string.unlock_message_secure_note_biometrics
            isSecureNote -> R.string.unlock_message_secure_note_master_password
            isBiometricUnlock -> R.string.unlock_message_item_biometrics
            else -> R.string.unlock_message_item_master_password
        }

        return context.getString(resId)
    }
}