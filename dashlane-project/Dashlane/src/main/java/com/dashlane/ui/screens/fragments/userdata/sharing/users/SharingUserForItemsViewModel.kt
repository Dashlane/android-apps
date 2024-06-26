package com.dashlane.ui.screens.fragments.userdata.sharing.users

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.sync.DataSync
import com.dashlane.events.AppEvents
import com.dashlane.events.SyncFinishedEvent
import com.dashlane.hermes.generated.definitions.Trigger
import com.dashlane.navigation.Navigator
import com.dashlane.server.api.endpoints.sharinguserdevice.ItemGroup
import com.dashlane.server.api.endpoints.sharinguserdevice.Permission
import com.dashlane.ui.screens.fragments.userdata.sharing.SharingModels
import com.dashlane.ui.screens.fragments.userdata.sharing.center.SharingDataProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SharingUserForItemsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val sharingDataProvider: SharingDataProvider,
    appEvents: AppEvents,
    private val sharingUsersDataProvider: SharingUsersDataProvider,
    private val navigator: Navigator,
    private val dataSync: DataSync
) : ViewModel(), SharingUsersForItemViewModelContract {
    override val itemId =
        SharingUserForItemsFragmentArgs.fromSavedStateHandle(savedStateHandle).argsItemUid

    override val uiState: MutableSharedFlow<SharingUsersForItemViewModelContract.UIState> =
        MutableSharedFlow(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    private val dataList = mutableListOf<SharingModels>()

    init {
        reloadData()
        appEvents.register(this, SyncFinishedEvent::class.java, false) {
            reloadData()
        }
    }

    override fun reloadData() {
        viewModelScope.launch {
            runCatching {
                sharingUsersDataProvider.getContactsForItem(itemId)
            }.onSuccess {
                dataList.apply {
                    clear()
                    addAll(it)
                    if (isEmpty()) {
                        navigator.popBackStack()
                    } else {
                        uiState.tryEmit(SharingUsersForItemViewModelContract.UIState.Data(it))
                    }
                }
            }.onFailure {
                navigator.popBackStack()
            }
        }
    }

    override fun pullToRefresh() = dataSync.sync(Trigger.MANUAL)

    override fun onResendInvite(itemGroup: ItemGroup, memberLogin: String) {
        uiState.tryEmit(SharingUsersForItemViewModelContract.UIState.RequestLoading)
        viewModelScope.launch {
            runCatching {
                sharingDataProvider.resendInvite(itemGroup, memberLogin)
            }.onFailure {
                uiState.tryEmit(SharingUsersForItemViewModelContract.UIState.RequestFailure)
            }.onSuccess {
                uiState.tryEmit(SharingUsersForItemViewModelContract.UIState.RequestSuccess)
                reloadData()
            }
        }
    }

    override fun onCancelInvite(itemGroupId: String, memberLogin: String) =
        onRevokeUser(itemGroupId, memberLogin)

    override fun onRevokeUser(itemGroupId: String, memberLogin: String) {
        val itemGroup = dataList.find { it.itemGroup.groupId == itemGroupId }?.itemGroup ?: return
        uiState.tryEmit(SharingUsersForItemViewModelContract.UIState.RequestLoading)
        viewModelScope.launch {
            runCatching {
                sharingDataProvider.cancelInvitation(
                    itemGroup,
                    listOf(memberLogin)
                )
            }.onFailure {
                uiState.tryEmit(SharingUsersForItemViewModelContract.UIState.RequestFailure)
            }.onSuccess {
                uiState.tryEmit(SharingUsersForItemViewModelContract.UIState.RequestSuccess)
                reloadData()
            }
        }
    }

    override fun onRevokeUserGroup(itemGroupId: String, userGroupId: String) {
        val itemGroup = dataList.find { it.itemGroup.groupId == itemGroupId }?.itemGroup ?: return
        uiState.tryEmit(SharingUsersForItemViewModelContract.UIState.RequestLoading)
        viewModelScope.launch {
            runCatching {
                sharingDataProvider.cancelInvitationUserGroups(
                    itemGroup,
                    listOf(userGroupId)
                )
            }.onFailure {
                uiState.tryEmit(SharingUsersForItemViewModelContract.UIState.RequestFailure)
            }.onSuccess {
                uiState.tryEmit(SharingUsersForItemViewModelContract.UIState.RequestSuccess)
                reloadData()
            }
        }
    }

    override fun onChangePermission(
        itemGroup: ItemGroup,
        newPermission: Permission,
        memberLogin: String
    ) {
        uiState.tryEmit(SharingUsersForItemViewModelContract.UIState.RequestLoading)
        viewModelScope.launch {
            runCatching {
                sharingDataProvider.updateItemGroupMember(
                    itemGroup,
                    newPermission,
                    memberLogin
                )
            }.onFailure {
                uiState.tryEmit(SharingUsersForItemViewModelContract.UIState.RequestFailure)
            }.onSuccess {
                uiState.tryEmit(SharingUsersForItemViewModelContract.UIState.RequestSuccess)
                reloadData()
            }
        }
    }

    override fun onChangePermissionUserGroup(
        itemGroup: ItemGroup,
        newPermission: Permission,
        userGroupId: String
    ) {
        uiState.tryEmit(SharingUsersForItemViewModelContract.UIState.RequestLoading)
        viewModelScope.launch {
            runCatching {
                sharingDataProvider.updateItemGroupMemberUserGroup(
                    itemGroup,
                    newPermission,
                    userGroupId
                )
            }.onFailure {
                uiState.tryEmit(SharingUsersForItemViewModelContract.UIState.RequestFailure)
            }.onSuccess {
                uiState.tryEmit(SharingUsersForItemViewModelContract.UIState.RequestSuccess)
                reloadData()
            }
        }
    }
}