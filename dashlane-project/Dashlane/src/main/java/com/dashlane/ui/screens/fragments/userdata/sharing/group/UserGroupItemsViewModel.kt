package com.dashlane.ui.screens.fragments.userdata.sharing.group

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.core.DataSync
import com.dashlane.events.AppEvents
import com.dashlane.events.SyncFinishedEvent
import com.dashlane.hermes.generated.definitions.Trigger
import com.dashlane.navigation.Navigator
import com.dashlane.server.api.endpoints.sharinguserdevice.ItemGroup
import com.dashlane.server.api.endpoints.sharinguserdevice.Permission
import com.dashlane.ui.screens.fragments.userdata.sharing.SharingModels
import com.dashlane.ui.screens.fragments.userdata.sharing.center.SharingDataProvider
import com.dashlane.xml.domain.SyncObjectType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserGroupItemsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val dataProvider: UserGroupDataProvider,
    private val sharingDataProvider: SharingDataProvider,
    appEvents: AppEvents,
    private val navigator: Navigator,
    private val dataSync: DataSync
) : ViewModel(), UserGroupItemsViewModelContract {

    override val userGroupId = savedStateHandle.get<String>(UserGroupItemsFragment.ARGS_GROUP_ID)!!

    override val uiState: MutableStateFlow<UserGroupItemsViewModelContract.UIState> =
        MutableStateFlow(UserGroupItemsViewModelContract.UIState.Loading)

    private val dataList = mutableListOf<SharingModels.ItemUserGroup>()

    init {
        reloadData()
        appEvents.register(this, SyncFinishedEvent::class.java, false) {
            reloadData()
        }
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
                    listOf(userGroupId)
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
                    userGroupId
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