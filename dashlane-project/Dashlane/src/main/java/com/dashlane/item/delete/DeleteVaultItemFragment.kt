package com.dashlane.item.delete

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dashlane.R
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.navigation.Navigator
import com.dashlane.util.Toaster
import com.dashlane.vault.item.delete.DeleteVaultItemLogger
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class DeleteVaultItemFragment : DialogFragment() {

    @Inject
    lateinit var navigator: Navigator

    @Inject
    lateinit var logger: DeleteVaultItemLogger

    @Inject
    lateinit var toaster: Toaster

    private val viewModel by viewModels<DeleteVaultItemViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                DashlaneTheme {
                    LaunchedEffect(key1 = viewModel) {
                        viewModel.stateFlow.sideEffect.collect { state ->
                            when (state) {
                                DeleteVaultItemViewModel.NavigationState.Failure -> deleteError()
                                is DeleteVaultItemViewModel.NavigationState.Success -> itemDeleted(state.itemId)
                            }
                        }
                    }
                    val state by viewModel.stateFlow.viewState.collectAsStateWithLifecycle()
                    DeleteVaultItemScreen(
                        state = state,
                        onConfirmed = { viewModel.deleteItem() },
                        onCancelled = { closeDialog() }
                    )
                }
            }
        }
    }

    private fun closeDialog() {
        logger.logItemDeletionCanceled()
        dismiss()
    }

    private fun itemDeleted(itemId: String) {
        toaster.show(R.string.item_deleted, Toast.LENGTH_SHORT)
        parentFragmentManager.setFragmentResult(
            DELETE_VAULT_ITEM_RESULT,
            bundleOf(DELETE_VAULT_ITEM_SUCCESS to true, DELETE_VAULT_ITEM_ID to itemId)
        )
        closeDialog()
    }

    private fun deleteError() {
        toaster.show(R.string.network_failed_notification, Toast.LENGTH_LONG)
        parentFragmentManager.setFragmentResult(
            DELETE_VAULT_ITEM_RESULT,
            bundleOf(DELETE_VAULT_ITEM_SUCCESS to false)
        )
        closeDialog()
    }

    companion object {
        const val DELETE_VAULT_ITEM_RESULT = "DELETE_VAULT_ITEM_RESULT"
        const val DELETE_VAULT_ITEM_SUCCESS = "DELETE_VAULT_ITEM_SUCCESS"
        const val DELETE_VAULT_ITEM_ID = "DELETE_VAULT_ITEM_ID"
        const val DELETE_DIALOG_TAG = "delete_confirm_dialog"
    }
}