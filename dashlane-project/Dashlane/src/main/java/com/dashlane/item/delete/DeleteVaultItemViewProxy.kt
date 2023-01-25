package com.dashlane.item.delete

import android.app.Activity
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.dashlane.R
import com.dashlane.item.delete.DeleteVaultItemFragment.Companion.DELETE_VAULT_ITEM_RESULT
import com.dashlane.item.delete.DeleteVaultItemFragment.Companion.DELETE_VAULT_ITEM_SUCCESS
import com.dashlane.ui.dialogs.fragment.WaiterDialogFragment
import com.dashlane.util.showToaster
import com.skocken.presentation.viewproxy.BaseViewProxy

class DeleteVaultItemViewProxy(
    activity: Activity,
    private val dialogFragment: DialogFragment,
    private val fragmentManager: FragmentManager
) : BaseViewProxy<DeleteVaultItemContract.Presenter>(activity), DeleteVaultItemContract.View {

    override fun itemDeleted() {
        context.showToaster(context.getString(R.string.item_deleted), Toast.LENGTH_SHORT)
        WaiterDialogFragment.dismissWaiter(fragmentManager)
        fragmentManager.setFragmentResult(DELETE_VAULT_ITEM_RESULT, bundleOf(DELETE_VAULT_ITEM_SUCCESS to true))
        dialogFragment.dismiss()
    }

    override fun deleteError() {
        context.showToaster(context.getString(R.string.network_failed_notification), Toast.LENGTH_LONG)
        WaiterDialogFragment.dismissWaiter(fragmentManager)
        fragmentManager.setFragmentResult(DELETE_VAULT_ITEM_RESULT, bundleOf(DELETE_VAULT_ITEM_SUCCESS to false))
        dialogFragment.dismiss()
    }
}