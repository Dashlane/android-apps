package com.dashlane.ui.screens.fragments.userdata.sharing.group

import com.dashlane.server.api.endpoints.sharinguserdevice.ItemGroup
import com.dashlane.server.api.endpoints.sharinguserdevice.Permission
import com.dashlane.ui.screens.fragments.userdata.sharing.SharingModels
import com.dashlane.xml.domain.SyncObjectType
import kotlinx.coroutines.flow.Flow

interface UserGroupItemsViewModelContract {
    val userGroupId: String
    val uiState: Flow<UIState>

    fun reloadData()

    fun pullToRefresh()

    fun onItemClicked(itemId: String, dataType: SyncObjectType)

    fun onRevokeUserGroup(itemGroupId: String)

    fun onChangePermission(itemGroup: ItemGroup, newPermission: Permission)

    sealed class UIState {
        object Loading : UIState()
        object Empty : UIState()
        object RequestLoading : UIState()
        object RequestSuccess : UIState()
        object RequestFailure : UIState()
        data class Data(
            val items: List<SharingModels.ItemUserGroup> =
                emptyList()
        ) : UIState()
    }
}
