package com.dashlane.ui.screens.fragments.userdata.sharing.center

import androidx.fragment.app.Fragment
import kotlinx.coroutines.flow.Flow

interface SharingCenterViewModelContract {
    val uiState: Flow<UIState>
    fun onClickNewShare(activity: Fragment)
    fun onUserClicked(user: SharingContact.User)
    fun onUserGroupClicked(userGroup: SharingContact.UserGroup)
    fun declineItemGroup(groupId: String)
    fun acceptItemGroup(invite: SharingContact.ItemInvite)
    fun declineUserGroup(userGroupId: String)
    fun acceptUserGroup(invite: SharingContact.UserGroupInvite)
    fun declineCollection(collectionId: String)
    fun acceptCollection(invite: SharingContact.CollectionInvite)

    fun reloadData()

    fun refresh()

    sealed class UIState {
        object Loading : UIState()
        object Empty : UIState()
        object RequestLoading : UIState()
        class RequestSuccess(val acceptedItemName: String? = null) : UIState()
        object RequestFailure : UIState()
        data class Data(
            val itemInvites: List<SharingContact.ItemInvite> = emptyList(),
            val userGroupInvites: List<SharingContact.UserGroupInvite> = emptyList(),
            val collectionInvites: List<SharingContact.CollectionInvite> = emptyList(),
            val users: List<SharingContact.User> = emptyList(),
            val userGroups: List<SharingContact.UserGroup> = emptyList()
        ) : UIState()
    }
}