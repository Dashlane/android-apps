package com.dashlane.ui.screens.fragments.userdata.sharing.items

import com.dashlane.server.api.endpoints.sharinguserdevice.ItemGroup
import com.dashlane.server.api.endpoints.sharinguserdevice.Permission
import com.dashlane.ui.screens.fragments.userdata.sharing.SharingModels
import com.dashlane.xml.domain.SyncObjectType
import kotlinx.coroutines.flow.Flow



interface SharingItemsForUserViewModelContract {
    val memberLogin: String
    val uiState: Flow<UIState>

    

    fun reloadData()

    

    fun pullToRefresh()

    fun onItemClicked(itemId: String, dataType: SyncObjectType)

    fun onResendInvite(itemGroup: ItemGroup)

    fun onCancelInvite(itemGroupId: String)

    fun onRevokeUser(itemGroupId: String)

    fun onChangePermission(itemGroup: ItemGroup, newPermission: Permission)

    sealed class UIState {
        object Loading : UIState()
        object RequestLoading : UIState()
        object RequestSuccess : UIState()
        object RequestFailure : UIState()
        data class Data(val items: List<SharingModels.ItemUser> = emptyList()) : UIState()
    }
}
