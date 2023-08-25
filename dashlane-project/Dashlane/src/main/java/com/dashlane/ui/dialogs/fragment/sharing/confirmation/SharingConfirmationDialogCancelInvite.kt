package com.dashlane.ui.dialogs.fragment.sharing.confirmation

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.dashlane.R
import com.dashlane.ui.dialogs.fragments.NotificationDialogFragment

class SharingConfirmationDialogCancelInvite :
    NotificationDialogFragment(),
    NotificationDialogFragment.TwoButtonClicker {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        clicker = this
        return super.onCreateDialog(savedInstanceState)
    }

    override fun onPositiveButton() {
        setFragmentResult(
            REQUEST_KEY,
            requireArguments()
        )
    }

    override fun onNegativeButton() = Unit

    companion object {
        val TAG: String = SharingConfirmationDialogCancelInvite::class.java.simpleName
        const val REQUEST_KEY = "cancel_invite_item_group_request_key"
        const val CANCEL_ITEM_GROUP_ID = "cancel_invite_item_group_id"
        const val CANCEL_FOR_USER_ID = "cancel_for_user_id"
        const val CANCEL_FOR_USER_GROUP_ID = "cancel_for_user_group_id"

        fun newInstanceForUser(
            context: Context,
            itemGroupId: String,
            userId: String,
        ): SharingConfirmationDialogCancelInvite =
            newInstance(context, itemGroupId, userId = userId)

        fun newInstanceForUserGroup(
            context: Context,
            itemGroupId: String,
            userGroupId: String,
        ): SharingConfirmationDialogCancelInvite =
            newInstance(context, itemGroupId, userGroupId = userGroupId)

        private fun newInstance(
            context: Context,
            itemGroupId: String,
            userId: String? = null,
            userGroupId: String? = null,
        ): SharingConfirmationDialogCancelInvite {
            return Builder().setArgs(
                bundleOf(
                    CANCEL_ITEM_GROUP_ID to itemGroupId,
                    CANCEL_FOR_USER_ID to userId,
                    CANCEL_FOR_USER_GROUP_ID to userGroupId
                )
            ).setTitle(context, R.string.sharing_confirmation_popup_title_cancel_invite)
                .setMessage(
                    context,
                    R.string.sharing_confirmation_popup_description_cancel_invite
                )
                .setPositiveButtonText(
                    context,
                    R.string.sharing_confirmation_popup_btn_confirm_cancel_invite
                )
                .setNegativeButtonText(
                    context,
                    R.string.sharing_confirmation_popup_btn_cancel_cancel_invite
                )
                .build(SharingConfirmationDialogCancelInvite())
        }
    }
}
