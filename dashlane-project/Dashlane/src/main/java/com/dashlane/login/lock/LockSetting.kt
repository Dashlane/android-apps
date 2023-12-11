package com.dashlane.login.lock

import android.os.Bundle
import android.os.Parcelable
import com.dashlane.lock.UnlockEvent
import com.dashlane.navigation.NavigationConstants
import com.dashlane.util.getParcelableCompat
import kotlinx.parcelize.Parcelize

@Parcelize
data class LockSetting(
    var isLoggedIn: Boolean = false,
    val unlockReason: UnlockEvent.Reason? = null,
    val isPinSetter: Boolean = false,
    val isLockCancelable: Boolean = false,
    val lockType: Int = LockTypeManager.LOCK_TYPE_UNSPECIFIED,
    val topicLock: String? = null,
    val subTopicLock: String? = null,
    val shouldThemeAsDialog: Boolean = false,
    val lockReferrer: String? = null,
    val lockWebsite: String? = null,
    val redirectToHome: Boolean = false,
    val allowBypass: Boolean = false
) : Parcelable {
    companion object {
        const val EXTRA_LOCK_TYPE_IS_PIN_SET = "extra_lock_type_is_pin_set"
        const val EXTRA_LOCK_TYPE = "extra_lock_type"
        const val EXTRA_SUB_TOPIC_LOCK = "extra_lock_sub_type"

        const val EXTRA_LOCK_REASON = "extra_lock_reason"
        const val EXTRA_IS_LOCK_CANCELABLE = "extra_is_lock_cancelable"
        const val EXTRA_TOPIC_LOCK = "extra_topic_lock"
        const val EXTRA_AS_DIALOG = "extra_as_dialog"
        const val EXTRA_DOMAIN = "extra_domain"

        const val EXTRA_REDIRECT_TO_HOME = "extra_redirect_to_home"

        @JvmStatic
        fun buildFrom(bundle: Bundle?): LockSetting = if (bundle != null) {
            val unlockReason = bundle.getParcelableCompat<UnlockEvent.Reason>(EXTRA_LOCK_REASON)
            LockSetting(
                unlockReason = unlockReason,
                isPinSetter = bundle.getBoolean(EXTRA_LOCK_TYPE_IS_PIN_SET, false),
                isLockCancelable = if (unlockReason != null) {
                    unlockReason !is UnlockEvent.Reason.AppAccess && unlockReason !is UnlockEvent.Reason.AccessFromAutofillApi
                } else {
                    bundle.getBoolean(EXTRA_IS_LOCK_CANCELABLE, false)
                },
                lockType = bundle.getInt(EXTRA_LOCK_TYPE, LockTypeManager.LOCK_TYPE_UNSPECIFIED),
                topicLock = bundle.getString(EXTRA_TOPIC_LOCK, null),
                subTopicLock = bundle.getString(EXTRA_SUB_TOPIC_LOCK, null),
                shouldThemeAsDialog = bundle.getBoolean(EXTRA_AS_DIALOG, false),
                lockReferrer = getLockReferrer(bundle),
                lockWebsite = bundle.getString(EXTRA_DOMAIN),
                redirectToHome = bundle.getBoolean(EXTRA_REDIRECT_TO_HOME, false)
            )
        } else {
            LockSetting()
        }

        private fun getLockReferrer(extras: Bundle): String? = when {
            extras.getBoolean(NavigationConstants.LOGIN_CALLED_FROM_INAPP_LOGIN, false) ->
                NavigationConstants.LOGIN_CALLED_FROM_INAPP_LOGIN
            else -> null
        }
    }
}