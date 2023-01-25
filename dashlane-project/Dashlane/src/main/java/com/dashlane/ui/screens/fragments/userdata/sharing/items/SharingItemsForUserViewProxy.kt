package com.dashlane.ui.screens.fragments.userdata.sharing.items

import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.dashlane.core.domain.sharing.toUserPermission
import com.dashlane.ui.adapter.util.populateItemsAsync
import com.dashlane.ui.dialogs.fragment.sharing.confirmation.SharingConfirmationDialogCancelInvite
import com.dashlane.ui.dialogs.fragment.sharing.confirmation.SharingConfirmationDialogRevoke
import com.dashlane.ui.dialogs.fragment.sharing.contextual.PopupMenuManageUserAccepted
import com.dashlane.ui.dialogs.fragment.sharing.contextual.PopupMenuManageUserPending
import com.dashlane.ui.screens.fragments.userdata.sharing.SharingBaseViewProxy
import com.dashlane.ui.screens.fragments.userdata.sharing.SharingModels
import com.dashlane.ui.screens.fragments.userdata.sharing.group.SharedVaultItemWrapper
import com.dashlane.ui.screens.fragments.userdata.sharing.group.addHeaders
import com.dashlane.ui.screens.fragments.userdata.sharing.onSuccess
import com.dashlane.ui.screens.fragments.userdata.sharing.showErrorDialog
import com.dashlane.ui.screens.fragments.userdata.sharing.showLoadingDialog
import com.dashlane.ui.setup
import com.skocken.efficientadapter.lib.adapter.EfficientAdapter
import kotlinx.coroutines.launch

class SharingItemsForUserViewProxy(
    fragment: Fragment,
    view: View,
    private val viewModel: SharingItemsForUserViewModelContract,
) : SharingBaseViewProxy(fragment, view) {

    init {
        refreshLayout.setup {
            viewModel.pullToRefresh()
        }
        coroutineScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                toolbar?.title = viewModel.memberLogin
                viewModel.reloadData()
                viewModel.uiState.collect { state ->
                    when (state) {
                        SharingItemsForUserViewModelContract.UIState.Loading ->
                            showLoadingState()
                        is SharingItemsForUserViewModelContract.UIState.Data ->
                            displayData(state)
                        SharingItemsForUserViewModelContract.UIState.RequestFailure ->
                            fragmentManager.showErrorDialog(context)
                        SharingItemsForUserViewModelContract.UIState.RequestLoading ->
                            fragmentManager.showLoadingDialog(context)
                        SharingItemsForUserViewModelContract.UIState.RequestSuccess ->
                            fragmentManager.onSuccess()
                    }
                }
            }
        }

        fragment.apply {
            setFragmentResultListener(
                SharingConfirmationDialogCancelInvite.REQUEST_KEY
            ) { _, bundle ->
                viewModel.onCancelInvite(
                    bundle.getString(SharingConfirmationDialogCancelInvite.CANCEL_ITEM_GROUP_ID)!!
                )
            }
            setFragmentResultListener(SharingConfirmationDialogRevoke.REQUEST_KEY) { _, bundle ->
                viewModel.onRevokeUser(
                    bundle.getString(SharingConfirmationDialogRevoke.REVOKE_ITEM_GROUP_ID)!!
                )
            }
        }
    }

    private suspend fun displayData(state: SharingItemsForUserViewModelContract.UIState.Data) {
        val listItems = state.items
            .map {
                SharedVaultItemWrapper(context, it,
                    onPendingMenuClick = { view, item ->
                        showManageSharingContactPending(view, item)
                    },
                    onAcceptedMenuClick = { view, item ->
                        showManageSharingContactAccepted(view, item)
                    })
            }
            .sortedWith(SharedVaultItemWrapper.comparator())
            .addHeaders(context)
        list.adapter?.apply {
            populateItemsAsync(listItems)
            onItemClickListener =
                EfficientAdapter.OnItemClickListener { _, _, item, _ ->
                    if (item is SharedVaultItemWrapper) {
                        val summaryObject = item.sharedItem.item
                        viewModel.onItemClicked(summaryObject.id, summaryObject.syncObjectType)
                    }
                }
        }
        loadingView.visibility = View.GONE
        refreshLayout.isRefreshing = false
        list.visibility = View.VISIBLE
    }

    private fun showManageSharingContactPending(
        view: View,
        sharedItem: SharingModels
    ) {
        val dialog = PopupMenuManageUserPending(context, view,
            onCancelInvite = {
                SharingConfirmationDialogCancelInvite.newInstanceForUser(
                    context, sharedItem.itemGroup.groupId, viewModel.memberLogin
                ).show(fragmentManager, SharingConfirmationDialogCancelInvite.TAG)
            },
            onResendInvite = {
                viewModel.onResendInvite(sharedItem.itemGroup)
            }
        )
        dialog.show()
    }

    private fun showManageSharingContactAccepted(
        view: View,
        sharedItem: SharingModels
    ) {
        val dialog = PopupMenuManageUserAccepted(context, view,
            isAdmin = sharedItem.isMemberAdmin,
            onAskRevokeUser = {
                SharingConfirmationDialogRevoke.newInstanceForUser(
                    context, sharedItem.itemGroup.groupId, viewModel.memberLogin
                ).show(fragmentManager, SharingConfirmationDialogRevoke.TAG)
            },
            onChangePermission = {
                viewModel.onChangePermission(sharedItem.itemGroup, it.toUserPermission())
            }
        )
        dialog.show()
    }
}
