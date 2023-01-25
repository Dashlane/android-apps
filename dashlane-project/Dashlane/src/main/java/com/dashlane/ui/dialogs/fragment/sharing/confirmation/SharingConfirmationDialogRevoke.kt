package com.dashlane.ui.dialogs.fragment.sharing.confirmation

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.dashlane.R
import com.dashlane.ui.dialogs.fragments.NotificationDialogFragment

class SharingConfirmationDialogRevoke : NotificationDialogFragment(),
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
        val TAG: String = SharingConfirmationDialogRevoke::class.java.simpleName
        const val REVOKE_ITEM_GROUP_ID = "revoke_itemgroup_id"
        const val REVOKE_FOR_USER_ID = "revoke_for_user_id"
        const val REVOKE_FOR_USER_GROUP_ID = "revoke_for_user_group_id"
        const val REQUEST_KEY = "sharing_confirmation_revoke_request_key"

        fun newInstanceForUser(
            context: Context,
            itemGroupId: String,
            userId: String,
        ): SharingConfirmationDialogRevoke =
            newInstance(
                context,
                itemGroupId,
                userId = userId
            )

        fun newInstanceForUserGroup(
            context: Context,
            itemGroupId: String,
            userGroupId: String,
        ): SharingConfirmationDialogRevoke =
            newInstance(
                context,
                itemGroupId,
                userGroupId = userGroupId
            )

        private fun newInstance(
            context: Context,
            itemGroupId: String,
            userId: String? = null,
            userGroupId: String? = null,
        ): SharingConfirmationDialogRevoke {
            return Builder().setArgs(
                bundleOf(
                    REVOKE_ITEM_GROUP_ID to itemGroupId,
                    REVOKE_FOR_USER_ID to userId,
                    REVOKE_FOR_USER_GROUP_ID to userGroupId
                )
            )
                .setTitle(
                    context,
                    R.string.sharing_confirmation_popup_title_revoke_item_from_sharing_center
                )
                .setMessage(
                    context,
                    R.string.sharing_confirmation_popup_description_revoke_item_from_sharing_center
                )
                .setPositiveButtonText(
                    context,
                    R.string.sharing_confirmation_popup_btn_confirm_revoke_item_from_sharing_center
                )
                .setNegativeButtonText(
                    context,
                    R.string.sharing_confirmation_popup_btn_cancel_revoke_item_from_sharing_center
                )
                .build(SharingConfirmationDialogRevoke())
        }
    }
}
