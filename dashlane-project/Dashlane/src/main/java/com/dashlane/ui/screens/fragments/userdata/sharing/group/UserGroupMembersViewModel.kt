package com.dashlane.ui.screens.fragments.userdata.sharing.group

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.hermes.generated.definitions.Trigger
import com.dashlane.session.SessionManager
import com.dashlane.sync.DataSync
import com.dashlane.sync.DataSyncState
import com.dashlane.ui.screens.fragments.userdata.sharing.SharingUserGroupUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserGroupMembersViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val dataProvider: UserGroupDataProvider,
    private val sessionManager: SessionManager,
    private val dataSync: DataSync,
) : ViewModel(), UserGroupMembersViewModelContract {
    private val userId: String?
        get() = sessionManager.session?.userId

    override val userGroupId =
        savedStateHandle.get<String>(UserGroupMembersFragment.ARGS_GROUP_ID)!!

    override val uiState: MutableSharedFlow<UserGroupMembersViewModelContract.UIState> =
        MutableSharedFlow(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    private val dataList = mutableListOf<SharingUserGroupUser>()

    init {
        reloadData()
        dataSync.dataSyncState
            .filterIsInstance<DataSyncState.Idle.Success>()
            .onEach { reloadData() }
            .launchIn(viewModelScope)
    }

    override fun reloadData() {
        val userId = userId ?: return
        viewModelScope.launch {
            runCatching {
                dataProvider.getMembersForUserGroup(userGroupId, userId)
            }.onSuccess {
                dataList.apply {
                    clear()
                    addAll(it)
                    if (isEmpty()) {
                        uiState.tryEmit(UserGroupMembersViewModelContract.UIState.Empty)
                    } else {
                        uiState.tryEmit(UserGroupMembersViewModelContract.UIState.Data(it))
                    }
                }
            }.onFailure {
                uiState.tryEmit(UserGroupMembersViewModelContract.UIState.Empty)
            }
        }
    }

    override fun pullToRefresh() = dataSync.sync(Trigger.MANUAL)
}