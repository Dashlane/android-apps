package com.dashlane.item.delete

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.dashlane.R
import com.dashlane.hermes.generated.definitions.AnyPage
import com.dashlane.ui.dialogs.fragment.WaiterDialogFragment
import com.dashlane.ui.util.DialogHelper
import com.dashlane.util.setCurrentPageView
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class DeleteVaultItemFragment : DialogFragment() {

    private var isShared: Boolean = false
    private var itemId: String = ""

    @Inject
    lateinit var provider: DeleteVaultItemProvider

    @Inject
    lateinit var deleteVaultItemLogger: DeleteVaultItemLogger
    private val presenter = DeleteVaultItemPresenter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.setCurrentPageView(AnyPage.CONFIRM_ITEM_DELETION)
        itemId = DeleteVaultItemFragmentArgs.fromBundle(requireArguments()).itemId
        isShared = DeleteVaultItemFragmentArgs.fromBundle(requireArguments()).isShared
        val viewProxy = DeleteVaultItemViewProxy(requireActivity(), this, parentFragmentManager)
        presenter.coroutineScope = lifecycleScope
        presenter.setProvider(provider)
        presenter.setView(viewProxy)
        viewProxy.setPresenter(presenter)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog: AlertDialog = DialogHelper().builder(requireActivity())
            .setTitle(
                if (isShared) {
                    resources.getString(R.string.sharing_confirmation_popup_title_delete_from_service)
                } else {
                    resources.getString(R.string.delete_item)
                }
            )
            .setMessage(
                if (isShared) {
                    resources.getString(R.string.sharing_confirmation_popup_description_delete_from_service)
                } else {
                    resources.getString(R.string.please_confirm_you_would_like_to_delete_the_item)
                }
            )
            .setPositiveButton(
                if (isShared) {
                    resources.getString(R.string.sharing_confirmation_popup_btn_confirm_delete_from_service)
                } else {
                    resources.getString(R.string.delete)
                },
                null
            )
            .setNegativeButton(
                if (isShared) {
                    resources.getString(R.string.sharing_confirmation_popup_btn_cancel_delete_from_service)
                } else {
                    resources.getString(R.string.cancel)
                }
            ) { _, _ -> deleteVaultItemLogger.logItemDeletionCanceled() }
            .create()
        dialog.setOnShowListener {
            onDialogShown(dialog)
        }
        return dialog
    }

    private fun onDialogShown(dialog: AlertDialog) {
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            WaiterDialogFragment.showWaiter(
                false,
                resources.getString(R.string.contacting_dashlane),
                resources.getString(R.string.contacting_dashlane),
                childFragmentManager
            )
            dialog.hide()
            deleteVaultItemLogger.logItemDeletionConfirmed()
            presenter.deleteItem(itemId)
        }
    }

    companion object {
        const val DELETE_VAULT_ITEM_RESULT = "DELETE_VAULT_ITEM_RESULT"
        const val DELETE_VAULT_ITEM_SUCCESS = "DELETE_VAULT_ITEM_SUCCESS"
        const val DELETE_DIALOG_TAG = "delete_confirm_dialog"
    }
}