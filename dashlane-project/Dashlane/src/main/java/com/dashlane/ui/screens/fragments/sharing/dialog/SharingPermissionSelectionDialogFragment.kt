package com.dashlane.ui.screens.fragments.sharing.dialog

import android.content.Context
import android.content.DialogInterface
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.dashlane.R
import com.dashlane.core.domain.sharing.SharingPermission
import com.dashlane.ui.dialogs.fragments.NotificationDialogFragment
import com.dashlane.ui.screens.sharing.NewSharePeopleViewProxy
import com.dashlane.ui.screens.sharing.NewSharePeopleViewProxy.Companion.CHANGE_PERMISSION_VALUE
import com.dashlane.util.getSerializableCompat

class SharingPermissionSelectionDialogFragment : NotificationDialogFragment() {
    override fun onPreCreateDialog(builder: AlertDialog.Builder) {
        super.onPreCreateDialog(builder)
        val sharingPermission = requireArguments().getSerializableCompat<SharingPermission>(ARGS_DEFAULT_PERMISSION)
        val items = arrayOf<CharSequence>(
            getString(SharingPermission.LIMITED.stringResource),
            getString(SharingPermission.ADMIN.stringResource)
        )
        val checkedItem = if (sharingPermission === SharingPermission.ADMIN) 1 else 0
        val adapter = ArrayAdapter(requireContext(), R.layout.simple_list_item_single_choice, items)
        builder.setSingleChoiceItems(
            adapter,
            checkedItem
        ) { dialogInterface: DialogInterface, which: Int ->
            setFragmentResult(
                NewSharePeopleViewProxy.CHANGE_PERMISSION_REQUEST_KEY,
                bundleOf(CHANGE_PERMISSION_VALUE to if (which == 1) SharingPermission.ADMIN else SharingPermission.LIMITED)
            )
            dialogInterface.dismiss()
        }
    }

    companion object {
        @JvmField
        val TAG: String = SharingPermissionSelectionDialogFragment::class.java.name
        const val ARGS_DEFAULT_PERMISSION = "args_default_permission"

        @JvmStatic
        fun newInstance(
            context: Context,
            defaultPermission: SharingPermission
        ): SharingPermissionSelectionDialogFragment {
            return Builder().setArgs(bundleOf(ARGS_DEFAULT_PERMISSION to defaultPermission))
                .setTitle(context, R.string.sharing_add_group_member_choose_permission)
                .build(SharingPermissionSelectionDialogFragment())
        }
    }
}
