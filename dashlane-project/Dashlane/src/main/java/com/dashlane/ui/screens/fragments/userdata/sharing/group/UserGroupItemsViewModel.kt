package com.dashlane.ui.screens.fragments.userdata.sharing.group

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.hermes.generated.definitions.Trigger
import com.dashlane.navigation.Navigator
import com.dashlane.server.api.endpoints.sharinguserdevice.ItemGroup
import com.dashlane.server.api.endpoints.sharinguserdevice.Permission
import com.dashlane.sharing.SharingDataProvider
import com.dashlane.sync.DataSync
import com.dashlane.sync.DataSyncState
import com.dashlane.ui.screens.fragments.userdata.sharing.SharingModels
import com.dashlane.xml.domain.SyncObjectType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserGroupItemsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val dataProvider: UserGroupDataProvider,
    private val sharingDataProvider: SharingDataProvider,
    private val navigator: Navigator,
    private val dataSync: DataSync,
) : ViewModel(), UserGroupItemsViewModelContract {
    override val userGroupId = savedStateHandle.get<String>(UserGroupItemsFragment.ARGS_GROUP_ID)!!

    override val uiState: MutableSharedFlow<UserGroupItemsViewModelContract.UIState> =
        MutableSharedFlow(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    private val dataList = mutableListOf<SharingModels.ItemUserGroup>()

    init {
        reloadData()
        dataSync.dataSyncState
            .filterIsInstance<DataSyncState.Idle.Success>()
            .onEach { reloadData() }
            .launchIn(viewModelScope)
    }

    override fun reloadData() {
        viewModelScope.launch {
            runCatching {
                dataProvider.getItemsForUserGroup(userGroupId)
            }.onSuccess {
                dataList.apply {
                    clear()
                    addAll(it)
                    if (isEmpty()) {
                        uiState.tryEmit(UserGroupItemsViewModelContract.UIState.Empty)
                    } else {
                        uiState.tryEmit(UserGroupItemsViewModelContract.UIState.Data(it))
                    }
                }
            }.onFailure {
                uiState.tryEmit(UserGroupItemsViewModelContract.UIState.Empty)
            }
        }
    }

    override fun onItemClicked(itemId: String, dataType: SyncObjectType) =
        navigator.goToItem(itemId, dataType.xmlObjectName)

    override fun pullToRefresh() = dataSync.sync(Trigger.MANUAL)

    override fun onRevokeUserGroup(itemGroupId: String) {
        val itemGroup = dataList.find { it.itemGroup.groupId == itemGroupId }?.itemGroup ?: return
        uiState.tryEmit(UserGroupItemsViewModelContract.UIState.RequestLoading)
        viewModelScope.launch {
            runCatching {
                sharingDataProvider.cancelInvitationUserGroups(
                    itemGroup,
                    listOf(userGroupId),
                )
            }.onFailure {
                uiState.tryEmit(UserGroupItemsViewModelContract.UIState.RequestFailure)
            }.onSuccess {
                uiState.tryEmit(UserGroupItemsViewModelContract.UIState.RequestSuccess)
                reloadData()
            }
        }
    }

    override fun onChangePermission(itemGroup: ItemGroup, newPermission: Permission) {
        uiState.tryEmit(UserGroupItemsViewModelContract.UIState.RequestLoading)
        viewModelScope.launch {
            runCatching {
                sharingDataProvider.updateItemGroupMemberUserGroup(
                    itemGroup,
                    newPermission,
                    userGroupId,
                )
            }.onFailure {
                uiState.tryEmit(UserGroupItemsViewModelContract.UIState.RequestFailure)
            }.onSuccess {
                uiState.tryEmit(UserGroupItemsViewModelContract.UIState.RequestSuccess)
                reloadData()
            }
        }
    }
}