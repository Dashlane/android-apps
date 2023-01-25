package com.dashlane.ui.screens.fragments.userdata.sharing.users

import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.dashlane.R
import com.dashlane.core.domain.sharing.toUserPermission
import com.dashlane.ui.adapter.DashlaneRecyclerAdapter
import com.dashlane.ui.adapter.HeaderItem
import com.dashlane.ui.adapter.util.populateItemsAsync
import com.dashlane.ui.dialogs.fragment.sharing.confirmation.SharingConfirmationDialogCancelInvite
import com.dashlane.ui.dialogs.fragment.sharing.confirmation.SharingConfirmationDialogRevoke
import com.dashlane.ui.dialogs.fragment.sharing.contextual.PopupMenuManageUserAccepted
import com.dashlane.ui.dialogs.fragment.sharing.contextual.PopupMenuManageUserPending
import com.dashlane.ui.screens.fragments.userdata.sharing.SharingBaseViewProxy
import com.dashlane.ui.screens.fragments.userdata.sharing.SharingContactItem
import com.dashlane.ui.screens.fragments.userdata.sharing.SharingModels
import com.dashlane.ui.screens.fragments.userdata.sharing.onSuccess
import com.dashlane.ui.screens.fragments.userdata.sharing.showErrorDialog
import com.dashlane.ui.screens.fragments.userdata.sharing.showLoadingDialog
import com.dashlane.ui.setup
import com.dashlane.vault.model.getUrlDisplayName
import com.dashlane.vault.model.urlForGoToWebsite
import com.dashlane.vault.summary.SummaryObject
import kotlinx.coroutines.launch

class SharingUsersForItemViewProxy(
    fragment: Fragment,
    view: View,
    private val viewModel: SharingUsersForItemViewModelContract,
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
                        SharingUsersForItemViewModelContract.UIState.Loading ->
                            showLoadingState()
                        is SharingUsersForItemViewModelContract.UIState.Data ->
                            displayData(state)
                        SharingUsersForItemViewModelContract.UIState.RequestFailure ->
                            fragmentManager.showErrorDialog(context)
                        SharingUsersForItemViewModelContract.UIState.RequestLoading ->
                            fragmentManager.showLoadingDialog(context)
                        SharingUsersForItemViewModelContract.UIState.RequestSuccess ->
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
                    bundle.getString(SharingConfirmationDialogCancelInvite.CANCEL_ITEM_GROUP_ID)!!,
                    bundle.getString(SharingConfirmationDialogCancelInvite.CANCEL_FOR_USER_ID)!!
                )
            }
            setFragmentResultListener(SharingConfirmationDialogRevoke.REQUEST_KEY) { _, bundle ->
                val itemGroupId =
                    bundle.getString(SharingConfirmationDialogRevoke.REVOKE_ITEM_GROUP_ID)!!
                bundle.getString(SharingConfirmationDialogRevoke.REVOKE_FOR_USER_ID)?.run {
                    viewModel.onRevokeUser(itemGroupId, this)
                }
                bundle.getString(SharingConfirmationDialogRevoke.REVOKE_FOR_USER_GROUP_ID)?.run {
                    viewModel.onRevokeUserGroup(itemGroupId, this)
                }
            }
        }
    }

    private suspend fun displayData(state: SharingUsersForItemViewModelContract.UIState.Data) {
        val summaryObject = state.items.first().item
        toolbar?.title = when (summaryObject) {
            is SummaryObject.Authentifiant -> summaryObject.title
                ?: summaryObject.urlForGoToWebsite?.getUrlDisplayName
            is SummaryObject.SecureNote -> summaryObject.title
            else -> null
        }

        val users =
            state.items.filterIsInstance<SharingModels.ItemUser>()
                .map {
                    SharedContactUser(context, it,
                        onPendingMenuClick = { view, item ->
                            showManageSharingContactPendingForUser(view, item)
                        },
                        onAcceptedMenuClick = { view, item ->
                            showManageSharingContactAcceptedForUser(view, item)
                        })
                }.sortedWith(SharingContactItem.comparator())

        val userGroups = state.items
            .filterIsInstance<SharingModels.ItemUserGroup>()
            .map {
                SharedContactUserGroup(context, it,
                    onPendingMenuClick = { view, item ->
                        showManageSharingContactPendingForUserGroup(view, item)
                    },
                    onAcceptedMenuClick = { view, item ->
                        showManageSharingContactAcceptedForUserGroup(view, item)
                    })
            }.sortedWith(SharingContactItem.comparator())

        val listItems = (userGroups + users).addHeaders()

        list.adapter?.apply {
            populateItemsAsync(listItems)
        }
        loadingView.visibility = View.GONE
        refreshLayout.isRefreshing = false
        list.visibility = View.VISIBLE
    }

    private fun List<DashlaneRecyclerAdapter.MultiColumnViewTypeProvider>.addHeaders(): List<DashlaneRecyclerAdapter.MultiColumnViewTypeProvider> {
        return toMutableList().apply {
            indexOfFirst { it is SharedContactUser }
                .takeIf { it != -1 }?.also {
                    add(
                        it, HeaderItem(context.getString(R.string.sharing_center_label_individuals))
                    )
                }

            indexOfFirst { it is SharedContactUserGroup }
                .takeIf { it != -1 }?.also {
                    add(it, HeaderItem(context.getString(R.string.sharing_center_label_groups)))
                }
        }
    }

    private fun showManageSharingContactPendingForUserGroup(
        view: View,
        sharedItem: SharingModels.ItemUserGroup
    ) {
        val dialog = PopupMenuManageUserPending(context, view,
            onCancelInvite = {
                SharingConfirmationDialogCancelInvite.newInstanceForUserGroup(
                    context, sharedItem.itemGroup.groupId, sharedItem.userGroup.groupId
                ).show(fragmentManager, SharingConfirmationDialogCancelInvite.TAG)
            },
            onResendInvite = {
                viewModel.onResendInvite(sharedItem.itemGroup, sharedItem.userGroup.groupId)
            }
        )
        dialog.show()
    }

    private fun showManageSharingContactPendingForUser(
        view: View,
        sharedItem: SharingModels.ItemUser
    ) {
        val dialog = PopupMenuManageUserPending(context, view,
            onCancelInvite = {
                SharingConfirmationDialogCancelInvite.newInstanceForUser(
                    context, sharedItem.itemGroup.groupId, sharedItem.user.userId
                ).show(fragmentManager, SharingConfirmationDialogCancelInvite.TAG)
            },
            onResendInvite = {
                viewModel.onResendInvite(sharedItem.itemGroup, sharedItem.user.userId)
            }
        )
        dialog.show()
    }

    private fun showManageSharingContactAcceptedForUser(
        view: View,
        sharedItem: SharingModels.ItemUser
    ) {
        val dialog = PopupMenuManageUserAccepted(context, view,
            isAdmin = sharedItem.isMemberAdmin,
            onAskRevokeUser = {
                SharingConfirmationDialogRevoke.newInstanceForUser(
                    context, sharedItem.itemGroup.groupId, sharedItem.user.userId
                ).show(fragmentManager, SharingConfirmationDialogRevoke.TAG)
            },
            onChangePermission = {
                viewModel.onChangePermission(
                    sharedItem.itemGroup,
                    it.toUserPermission(),
                    sharedItem.user.userId
                )
            }
        )
        dialog.show()
    }

    private fun showManageSharingContactAcceptedForUserGroup(
        view: View,
        sharedItem: SharingModels.ItemUserGroup
    ) {
        val dialog = PopupMenuManageUserAccepted(context, view,
            isAdmin = sharedItem.isMemberAdmin,
            onAskRevokeUser = {
                SharingConfirmationDialogRevoke.newInstanceForUserGroup(
                    context, sharedItem.itemGroup.groupId, sharedItem.userGroup.groupId
                ).show(fragmentManager, SharingConfirmationDialogRevoke.TAG)
            },
            onChangePermission = {
                viewModel.onChangePermissionUserGroup(
                    sharedItem.itemGroup,
                    it.toUserPermission(),
                    sharedItem.userGroup.groupId
                )
            }
        )
        dialog.show()
    }
}
