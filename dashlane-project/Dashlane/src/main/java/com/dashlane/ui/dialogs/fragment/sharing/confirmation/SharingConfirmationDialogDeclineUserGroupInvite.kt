package com.dashlane.ui.dialogs.fragment.sharing.confirmation

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.dashlane.R
import com.dashlane.ui.dialogs.fragments.NotificationDialogFragment

class SharingConfirmationDialogDeclineUserGroupInvite :
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
        val TAG: String = SharingConfirmationDialogDeclineUserGroupInvite::class.java.simpleName
        const val REQUEST_KEY = "decline_user_group_invite_request_key"
        const val DECLINE_USER_GROUP_ID = "decline_user_group_id"

        @JvmStatic
        fun newInstance(
            context: Context,
            userGroupId: String
        ): SharingConfirmationDialogDeclineUserGroupInvite {
            return Builder().setArgs(bundleOf(DECLINE_USER_GROUP_ID to userGroupId))
                .setTitle(context, R.string.sharing_confirmation_popup_title_decline_group_invite)
                .setMessage(
                    context,
                    R.string.sharing_confirmation_popup_description_decline_user_group_invite
                )
                .setPositiveButtonText(
                    context,
                    R.string.sharing_confirmation_popup_btn_confirm_decline_group_invite
                )
                .setNegativeButtonText(
                    context,
                    R.string.sharing_confirmation_popup_btn_cancel_decline_group_invite
                )
                .build(SharingConfirmationDialogDeclineUserGroupInvite())
        }
    }
}
