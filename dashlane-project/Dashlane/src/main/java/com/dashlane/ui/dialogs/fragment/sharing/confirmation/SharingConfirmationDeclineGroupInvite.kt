package com.dashlane.ui.dialogs.fragment.sharing.confirmation

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.dashlane.R
import com.dashlane.ui.dialogs.fragments.NotificationDialogFragment

class SharingConfirmationDeclineGroupInvite :
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
        val TAG: String = SharingConfirmationDeclineGroupInvite::class.java.simpleName
        const val REQUEST_KEY = "decline_item_group_invite_request_key"
        const val DECLINE_ITEM_GROUP_ID = "decline_item_group_id"

        @JvmStatic
        fun newInstance(
            context: Context,
            itemGroupId: String
        ): SharingConfirmationDeclineGroupInvite {
            return Builder()
                .setArgs(bundleOf(DECLINE_ITEM_GROUP_ID to itemGroupId))
                .setTitle(context, R.string.sharing_confirmation_popup_title_decline_group_invite)
                .setMessage(
                    context,
                    R.string.sharing_confirmation_popup_description_decline_group_invite
                )
                .setPositiveButtonText(
                    context,
                    R.string.sharing_confirmation_popup_btn_confirm_decline_group_invite
                )
                .setNegativeButtonText(
                    context,
                    R.string.sharing_confirmation_popup_btn_cancel_cancel_invite
                )
                .build(SharingConfirmationDeclineGroupInvite())
        }
    }
}
