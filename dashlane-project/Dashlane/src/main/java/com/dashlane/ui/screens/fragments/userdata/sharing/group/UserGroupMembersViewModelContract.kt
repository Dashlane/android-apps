package com.dashlane.ui.screens.fragments.userdata.sharing.group

import com.dashlane.ui.screens.fragments.userdata.sharing.SharingUserGroupUser
import kotlinx.coroutines.flow.Flow

interface UserGroupMembersViewModelContract {
    val userGroupId: String
    val uiState: Flow<UIState>

    fun reloadData()

    fun pullToRefresh()

    sealed class UIState {
        object Loading : UIState()
        object Empty : UIState()
        object RequestLoading : UIState()
        object RequestSuccess : UIState()
        object RequestFailure : UIState()
        data class Data(
            val items: List<SharingUserGroupUser> = emptyList()
        ) : UIState()
    }
}
