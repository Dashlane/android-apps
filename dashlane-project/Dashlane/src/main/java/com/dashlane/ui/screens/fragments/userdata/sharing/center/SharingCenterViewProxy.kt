package com.dashlane.ui.screens.fragments.userdata.sharing.center

import android.content.Context
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.dashlane.R
import com.dashlane.ui.activities.fragments.list.wrapper.ItemWrapperProvider
import com.dashlane.ui.adapter.DashlaneRecyclerAdapter.ViewTypeProvider
import com.dashlane.ui.adapter.HeaderItem
import com.dashlane.ui.adapter.util.populateItemsAsync
import com.dashlane.ui.dialogs.fragment.sharing.confirmation.SharingConfirmationDeclineGroupInvite
import com.dashlane.ui.dialogs.fragment.sharing.confirmation.SharingConfirmationDialogDeclineUserGroupInvite
import com.dashlane.ui.screens.fragments.userdata.sharing.SharingBaseViewProxy
import com.dashlane.ui.screens.fragments.userdata.sharing.SharingContactItem
import com.dashlane.ui.screens.fragments.userdata.sharing.onSuccess
import com.dashlane.ui.screens.fragments.userdata.sharing.showErrorDialog
import com.dashlane.ui.screens.fragments.userdata.sharing.showLoadingDialog
import com.dashlane.ui.setup
import com.dashlane.ui.widgets.view.empty.SharingCenterEmptyScreen
import com.dashlane.util.SnackbarUtils
import com.skocken.efficientadapter.lib.adapter.EfficientAdapter
import kotlinx.coroutines.launch

class SharingCenterViewProxy(
    private val fragment: Fragment,
    view: View,
    private val viewModel: SharingCenterViewModelContract,
    private val itemWrapperProvider: ItemWrapperProvider
) : SharingBaseViewProxy(fragment, view) {
    init {
        fabButton.apply {
            visibility = View.VISIBLE
            setOnClickListener {
                viewModel.onClickNewShare(fragment)
            }
        }

        refreshLayout.setup {
            viewModel.refresh()
        }
        list.addOnScrollListener(fabButton.onScrollListener)

        fragment.apply {
            setFragmentResultListener(SharingConfirmationDeclineGroupInvite.REQUEST_KEY) { _, bundle ->
                viewModel.declineItemGroup(
                    bundle.getString(SharingConfirmationDeclineGroupInvite.DECLINE_ITEM_GROUP_ID)!!
                )
            }
            setFragmentResultListener(SharingConfirmationDialogDeclineUserGroupInvite.REQUEST_KEY) { _, bundle ->
                viewModel.declineUserGroup(
                    bundle.getString(SharingConfirmationDialogDeclineUserGroupInvite.DECLINE_USER_GROUP_ID)!!
                )
            }
        }

        coroutineScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.reloadData()
                viewModel.uiState.collect { state ->
                    when (state) {
                        SharingCenterViewModelContract.UIState.Loading ->
                            showLoadingState()
                        is SharingCenterViewModelContract.UIState.Data ->
                            displayData(state)
                        SharingCenterViewModelContract.UIState.Empty -> {
                            displayEmptyView()
                            showEmptyState()
                        }
                        SharingCenterViewModelContract.UIState.RequestFailure -> fragmentManager.showErrorDialog(
                            context
                        )
                        SharingCenterViewModelContract.UIState.RequestLoading -> fragmentManager.showLoadingDialog(
                            context
                        )
                        is SharingCenterViewModelContract.UIState.RequestSuccess -> {
                            fragmentManager.onSuccess()
                            state.acceptedItemName?.let {
                                SnackbarUtils.showSnackbar(
                                    view,
                                    context.getString(R.string.sharing_invite_item_accepted, it)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private suspend fun displayData(state: SharingCenterViewModelContract.UIState.Data) {
        val vaultItemsList = mutableListOf<ViewTypeProvider>()

        val users = state.users.toSharingContactDirectCountItems()
        val userGroups = state.userGroups.toSharingUserGroupItems()
        val itemInvites = state.itemInvites.toSharingInvitationItems()
        val groupInvites = state.userGroupInvites.toSharingInvitationUserGroups()
        val collectionInvites = state.collectionInvites.toSharingInvitationCollection()

        vaultItemsList.apply {
            if (itemInvites.isNotEmpty() || groupInvites.isNotEmpty() || collectionInvites.isNotEmpty()) {
                add(getHeaderInvites(context))
                addAll(groupInvites)
                addAll(collectionInvites)
                addAll(itemInvites)
            }
            if (userGroups.isNotEmpty()) {
                add(getHeaderGroup(context))
                addAll(userGroups)
            }
            if (users.isNotEmpty()) {
                add(getHeaderIndividual(context))
                addAll(users)
            }
        }
        list.adapter?.apply {
            populateItemsAsync(vaultItemsList)
            onItemClickListener =
                EfficientAdapter.OnItemClickListener { _, _, item, _ ->
                    if (item is SharingCenterUserGroupItem) {
                        viewModel.onUserGroupClicked(item.userGroup)
                    } else if (item is SharingCenterUserItem) {
                        viewModel.onUserClicked(item.user)
                    }
                }
        }
        loadingView.visibility = View.GONE
        refreshLayout.isRefreshing = false
        list.visibility = View.VISIBLE
    }

    private fun List<SharingContact.UserGroupInvite>.toSharingInvitationUserGroups() =
        map { invite ->
            SharingInvitationUserGroup(
                context,
                invite,
                onClickAccept = { viewModel.acceptUserGroup(invite) },
                onClickDecline = {
                    SharingConfirmationDialogDeclineUserGroupInvite.newInstance(
                        context,
                        invite.userGroup.groupId
                    ).show(fragmentManager, SharingConfirmationDialogDeclineUserGroupInvite.TAG)
                },
            )
        }.sortedWith(SharingInvitationUserGroup.comparator())

    private fun List<SharingContact.CollectionInvite>.toSharingInvitationCollection() =
        map { invite ->
            SharingInvitationCollection(
                context,
                invite,
                onClickAccept = { viewModel.acceptCollection(invite) },
                onClickDecline = { viewModel.declineCollection(invite.collection.uuid) },
            )
        }.sortedWith(SharingInvitationCollection.comparator())

    private fun List<SharingContact.ItemInvite>.toSharingInvitationItems() = map { invite ->
        SharingInvitationItem(
            context,
            itemWrapperProvider,
            invite,
            onClickAccept = {
                viewModel.acceptItemGroup(invite)
            },
            onClickDecline = {
                SharingConfirmationDeclineGroupInvite.newInstance(
                    context,
                    invite.itemGroup.groupId
                ).show(fragmentManager, SharingConfirmationDeclineGroupInvite.TAG)
            },
        )
    }.sortedWith(SharingInvitationItem.comparator())

    private fun List<SharingContact.UserGroup>.toSharingUserGroupItems() = map {
        SharingCenterUserGroupItem(context, it)
    }.sortedWith(SharingContactItem.comparator())

    private fun List<SharingContact.User>.toSharingContactDirectCountItems() = map {
        SharingCenterUserItem(context, it)
    }.sortedWith(SharingContactItem.comparator())

    private fun displayEmptyView() {
        list.adapter?.apply {
            clear()
            add(SharingCenterEmptyScreen.newInstance(context))
        }
    }

    private fun getHeaderInvites(context: Context) =
        HeaderItem(context.getString(R.string.sharing_center_label_pending_invitation))

    private fun getHeaderIndividual(context: Context) =
        HeaderItem(context.getString(R.string.sharing_center_label_individuals))

    private fun getHeaderGroup(context: Context) =
        HeaderItem(context.getString(R.string.sharing_center_label_groups))
}
