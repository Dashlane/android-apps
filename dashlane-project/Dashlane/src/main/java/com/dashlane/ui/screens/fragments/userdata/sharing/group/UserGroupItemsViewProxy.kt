package com.dashlane.ui.screens.fragments.userdata.sharing.group

import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.dashlane.R
import com.dashlane.core.domain.sharing.toUserPermission
import com.dashlane.sharing.model.isUserGroupSolitaryAdmin
import com.dashlane.ui.adapter.util.populateItemsAsync
import com.dashlane.ui.dialogs.fragment.sharing.confirmation.SharingConfirmationDialogRevoke
import com.dashlane.ui.dialogs.fragment.sharing.contextual.PopupMenuManageUserAccepted
import com.dashlane.ui.screens.fragments.userdata.sharing.SharingBaseViewProxy
import com.dashlane.ui.screens.fragments.userdata.sharing.SharingModels
import com.dashlane.ui.screens.fragments.userdata.sharing.onSuccess
import com.dashlane.ui.screens.fragments.userdata.sharing.showErrorDialog
import com.dashlane.ui.screens.fragments.userdata.sharing.showLoadingDialog
import com.dashlane.ui.setup
import com.dashlane.ui.util.DialogHelper
import com.dashlane.ui.widgets.view.empty.UserGroupItemListEmptyScreen
import com.skocken.efficientadapter.lib.adapter.EfficientAdapter
import kotlinx.coroutines.launch

class UserGroupItemsViewProxy(
    fragment: Fragment,
    view: View,
    private val viewModel: UserGroupItemsViewModelContract,
) : SharingBaseViewProxy(fragment, view) {

    init {
        refreshLayout.setup {
            viewModel.pullToRefresh()
        }
        coroutineScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.reloadData()
                viewModel.uiState.collect { state ->
                    when (state) {
                        UserGroupItemsViewModelContract.UIState.Loading ->
                            showLoadingState()
                        is UserGroupItemsViewModelContract.UIState.Data ->
                            displayData(state)
                        UserGroupItemsViewModelContract.UIState.Empty -> {
                            displayEmptyView()
                            showEmptyState()
                        }
                        UserGroupItemsViewModelContract.UIState.RequestFailure ->
                            fragmentManager.showErrorDialog(context)
                        UserGroupItemsViewModelContract.UIState.RequestLoading ->
                            fragmentManager.showLoadingDialog(context)
                        UserGroupItemsViewModelContract.UIState.RequestSuccess ->
                            fragmentManager.onSuccess()
                    }
                }
            }
        }

        fragment.apply {
            setFragmentResultListener(SharingConfirmationDialogRevoke.REQUEST_KEY) { _, bundle ->
                viewModel.onRevokeUserGroup(
                    bundle.getString(SharingConfirmationDialogRevoke.REVOKE_ITEM_GROUP_ID)!!
                )
            }
        }
    }

    private suspend fun displayData(state: UserGroupItemsViewModelContract.UIState.Data) {
        val listItems = state.items
            .map {
                SharedVaultItemWrapper(
                    context,
                    it,
                    onPendingMenuClick = { _, _ -> },
                    onAcceptedMenuClick = { view, item ->
                        
                        if (item.itemGroup.isUserGroupSolitaryAdmin(viewModel.userGroupId)) {
                            showDialogSolitaryAdmin()
                        } else {
                            showManageSharingContactAccepted(view, item)
                        }
                    }
                )
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

    private fun showDialogSolitaryAdmin() {
        DialogHelper()
            .builder(context)
            .setMessage(R.string.user_group_edit_item_last_admin_warning_message)
            .setCancelable(true)
            .show()
    }

    private fun showManageSharingContactAccepted(
        view: View,
        sharedItem: SharingModels
    ) {
        val dialog = PopupMenuManageUserAccepted(
            context,
            view,
            isAdmin = sharedItem.isMemberAdmin,
            onAskRevokeUser = {
                SharingConfirmationDialogRevoke.newInstanceForUserGroup(
                    context,
                    sharedItem.itemGroup.groupId,
                    viewModel.userGroupId
                ).show(fragmentManager, SharingConfirmationDialogRevoke.TAG)
            },
            onChangePermission = {
                viewModel.onChangePermission(sharedItem.itemGroup, it.toUserPermission())
            }
        )
        dialog.show()
    }

    private fun displayEmptyView() {
        list.adapter?.apply {
            clear()
            add(UserGroupItemListEmptyScreen.newInstance(context))
        }
    }
}
