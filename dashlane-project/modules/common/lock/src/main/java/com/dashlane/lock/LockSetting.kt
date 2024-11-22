package com.dashlane.lock

import android.os.Bundle
import android.os.Parcelable
import com.dashlane.util.getParcelableCompat
import kotlinx.parcelize.Parcelize

sealed interface LockPrompt : Parcelable {
    @Parcelize
    data object Regular : LockPrompt

    @Parcelize
    data class ForItem(val isSecureNote: Boolean = false) : LockPrompt

    @Parcelize
    data object ForSettings : LockPrompt
}

@Parcelize
data class LockSetting(
    var isLoggedIn: Boolean = false,
    val unlockReason: LockEvent.Unlock.Reason? = null,
    val isLockCancelable: Boolean = false,
    val isShowMPForRemember: Boolean = false,
    val lockPrompt: LockPrompt = LockPrompt.Regular,
    val locks: List<LockType> = emptyList(),
    val shouldThemeAsDialog: Boolean = false,
    val lockWebsite: String? = null,
    val redirectToHome: Boolean = false,
    val allowBypass: Boolean = false,
    val isMasterPasswordReset: Boolean = false,
) : Parcelable {
    companion object {
        const val EXTRA_LOCK_PROMPT = "extra_lock_prompt"

        const val EXTRA_LOCK_REASON = "extra_lock_reason"
        const val EXTRA_IS_LOCK_CANCELABLE = "extra_is_lock_cancelable"
        const val EXTRA_AS_DIALOG = "extra_as_dialog"
        const val EXTRA_DOMAIN = "extra_domain"

        const val EXTRA_REDIRECT_TO_HOME = "extra_redirect_to_home"
        const val EXTRA_IS_MASTER_PASSWORD_RESET = "extra_is_master_password_reset"

        @JvmStatic
        fun buildFrom(bundle: Bundle?): LockSetting = if (bundle != null) {
            val unlockReason = bundle.getParcelableCompat<LockEvent.Unlock.Reason>(EXTRA_LOCK_REASON)
            LockSetting(
                unlockReason = unlockReason,
                isLockCancelable = if (unlockReason != null) {
                    unlockReason !is LockEvent.Unlock.Reason.AppAccess && unlockReason !is LockEvent.Unlock.Reason.AccessFromAutofillApi
                } else {
                    bundle.getBoolean(EXTRA_IS_LOCK_CANCELABLE, false)
                },
                lockPrompt = bundle.getParcelableCompat<LockPrompt>(EXTRA_LOCK_PROMPT) ?: LockPrompt.Regular,
                shouldThemeAsDialog = bundle.getBoolean(EXTRA_AS_DIALOG, false),
                lockWebsite = bundle.getString(EXTRA_DOMAIN),
                redirectToHome = bundle.getBoolean(EXTRA_REDIRECT_TO_HOME, false),
                isMasterPasswordReset = bundle.getBoolean(EXTRA_IS_MASTER_PASSWORD_RESET, false)
            )
        } else {
            LockSetting()
        }
    }
}