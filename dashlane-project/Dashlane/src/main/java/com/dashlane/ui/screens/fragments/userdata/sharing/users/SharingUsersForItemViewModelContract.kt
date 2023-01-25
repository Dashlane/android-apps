package com.dashlane.ui.screens.fragments.userdata.sharing.users

import com.dashlane.server.api.endpoints.sharinguserdevice.ItemGroup
import com.dashlane.server.api.endpoints.sharinguserdevice.Permission
import com.dashlane.ui.screens.fragments.userdata.sharing.SharingModels
import kotlinx.coroutines.flow.Flow



interface SharingUsersForItemViewModelContract {
    val itemId: String
    val uiState: Flow<UIState>

    

    fun reloadData()

    

    fun pullToRefresh()

    fun onResendInvite(itemGroup: ItemGroup, memberLogin: String)

    fun onCancelInvite(itemGroupId: String, memberLogin: String)

    fun onRevokeUser(itemGroupId: String, memberLogin: String)

    fun onRevokeUserGroup(itemGroupId: String, userGroupId: String)

    fun onChangePermission(
        itemGroup: ItemGroup,
        newPermission: Permission,
        memberLogin: String
    )

    fun onChangePermissionUserGroup(
        itemGroup: ItemGroup,
        newPermission: Permission,
        userGroupId: String
    )

    sealed class UIState {
        object Loading : UIState()
        object RequestLoading : UIState()
        object RequestSuccess : UIState()
        object RequestFailure : UIState()
        data class Data(val items: List<SharingModels> = emptyList()) : UIState()
    }
}
