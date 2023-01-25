package com.dashlane.ui.screens.fragments.userdata.sharing.group

import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.dashlane.ui.adapter.util.populateItemsAsync
import com.dashlane.ui.screens.fragments.userdata.sharing.SharingBaseViewProxy
import com.dashlane.ui.screens.fragments.userdata.sharing.SharingContactItem
import com.dashlane.ui.screens.fragments.userdata.sharing.onSuccess
import com.dashlane.ui.screens.fragments.userdata.sharing.showErrorDialog
import com.dashlane.ui.screens.fragments.userdata.sharing.showLoadingDialog
import com.dashlane.ui.setup
import com.dashlane.ui.widgets.view.empty.UserGroupMemberListEmptyScreen
import kotlinx.coroutines.launch

class UserGroupMembersViewProxy(
    fragment: Fragment,
    view: View,
    private val viewModel: UserGroupMembersViewModelContract,
) : SharingBaseViewProxy(fragment, view) {

    init {
        refreshLayout.setup { viewModel.pullToRefresh() }
        coroutineScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.reloadData()
                viewModel.uiState.collect { state ->
                    when (state) {
                        UserGroupMembersViewModelContract.UIState.Loading ->
                            showLoadingState()
                        is UserGroupMembersViewModelContract.UIState.Data ->
                            displayData(state)
                        UserGroupMembersViewModelContract.UIState.Empty -> {
                            displayEmptyView()
                            showEmptyState()
                        }
                        UserGroupMembersViewModelContract.UIState.RequestFailure ->
                            fragmentManager.showErrorDialog(context)
                        UserGroupMembersViewModelContract.UIState.RequestLoading ->
                            fragmentManager.showLoadingDialog(context)
                        UserGroupMembersViewModelContract.UIState.RequestSuccess ->
                            fragmentManager.onSuccess()
                    }
                }
            }
        }
    }

    private suspend fun displayData(state: UserGroupMembersViewModelContract.UIState.Data) {
        val listItems = state.items
            .map {
                UserGroupMemberItem(context, it)
            }.sortedWith(SharingContactItem.comparator())
        list.adapter?.apply {
            populateItemsAsync(listItems)
        }
        loadingView.visibility = View.GONE
        refreshLayout.isRefreshing = false
        list.visibility = View.VISIBLE
    }

    private fun displayEmptyView() {
        list.adapter?.apply {
            clear()
            add(UserGroupMemberListEmptyScreen.newInstance(context))
        }
    }
}
