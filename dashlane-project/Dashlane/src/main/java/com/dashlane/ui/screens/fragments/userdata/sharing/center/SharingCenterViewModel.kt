package com.dashlane.ui.screens.fragments.userdata.sharing.center

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.core.DataSync
import com.dashlane.events.AppEvents
import com.dashlane.events.SyncFinishedEvent
import com.dashlane.hermes.generated.definitions.Trigger
import com.dashlane.navigation.Navigator
import com.dashlane.server.api.endpoints.sharinguserdevice.ItemGroup
import com.dashlane.server.api.endpoints.sharinguserdevice.UserGroup
import com.dashlane.session.Session
import com.dashlane.session.SessionManager
import com.dashlane.session.repository.TeamspaceManagerRepository
import com.dashlane.sharing.model.getUser
import com.dashlane.sharing.model.isAccepted
import com.dashlane.teamspaces.manager.TeamspaceAccessor.FeatureCall
import com.dashlane.teamspaces.manager.TeamspaceManager
import com.dashlane.teamspaces.model.Teamspace
import com.dashlane.useractivity.log.usage.UsageLogCode80
import com.dashlane.vault.summary.SummaryObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SharingCenterViewModel @Inject constructor(
    private val dataProvider: SharingDataProvider,
    private val creator: SharingContactCreator,
    private val sessionManager: SessionManager,
    private val teamspaceRepository: TeamspaceManagerRepository,
    private val navigator: Navigator,
    private val appEvents: AppEvents,
    private val dataSync: DataSync
) : ViewModel(), SharingCenterViewModelContract, TeamspaceManager.Listener {
    private val session: Session?
        get() = sessionManager.session

    private val teamspaceManager: TeamspaceManager?
        get() = teamspaceRepository[session]

    private val dataFlow =
        MutableStateFlow<Pair<List<ItemGroup>, List<UserGroup>>>(Pair(emptyList(), emptyList()))

    private val uiStateData = MutableStateFlow(SharingCenterViewModelContract.UIState.Data())

    override val uiState: MutableStateFlow<SharingCenterViewModelContract.UIState> =
        MutableStateFlow(SharingCenterViewModelContract.UIState.Loading)

    init {
        teamspaceManager?.subscribeListener(this)
        appEvents.register(this, SyncFinishedEvent::class.java, false) {
            reloadData()
        }
        viewModelScope.launch {
            dataFlow.collect { (itemGroups, userGroups) ->
                display(itemGroups, userGroups)
            }
        }
        reloadData()
    }

    override fun onCleared() {
        super.onCleared()
        teamspaceManager?.unSubscribeListeners(this)
        appEvents.unregister(this, SyncFinishedEvent::class.java)
    }

    override fun declineItemGroup(groupId: String) {
        val item = uiStateData.value.itemInvites.find { it.itemGroup.groupId == groupId } ?: return
        uiState.tryEmit(SharingCenterViewModelContract.UIState.RequestLoading)
        sendDeclineItemGroupRequest(itemGroup = item.itemGroup, summaryObject = item.item)
    }

    private fun sendDeclineItemGroupRequest(
        itemGroup: ItemGroup,
        summaryObject: SummaryObject
    ) {
        viewModelScope.launch {
            runCatching {
                dataProvider.declineItemGroupInvite(itemGroup, summaryObject, true)
            }.onFailure {
                uiState.tryEmit(SharingCenterViewModelContract.UIState.RequestFailure)
            }.onSuccess {
                uiState.tryEmit(SharingCenterViewModelContract.UIState.RequestSuccess)
                reloadData()
            }
        }
    }

    override fun declineUserGroup(userGroupId: String) {
        val userGroup =
            uiStateData.value.userGroupInvites.find { it.userGroup.groupId == userGroupId }
                ?: return
        uiState.tryEmit(SharingCenterViewModelContract.UIState.RequestLoading)
        sendDeclineUserGroupRequest(userGroup.userGroup)
    }

    private fun sendDeclineUserGroupRequest(userGroup: UserGroup) {
        viewModelScope.launch {
            runCatching {
                dataProvider.declineUserGroupInvite(userGroup, true)
            }.onFailure {
                uiState.tryEmit(SharingCenterViewModelContract.UIState.RequestFailure)
            }.onSuccess {
                uiState.tryEmit(SharingCenterViewModelContract.UIState.RequestSuccess)
                reloadData()
            }
        }
    }

    private fun display(itemGroups: List<ItemGroup>, userGroups: List<UserGroup>) {
        val login = session?.username?.email ?: return
        val myUserGroups = userGroups.filter { it.getUser(login)?.isAccepted == true }
        val myUserGroupsIds = myUserGroups.map { it.groupId }
        val myItemGroups = itemGroups.filter { itemGroup ->
            itemGroup.getUser(login)?.isAccepted == true || itemGroup.groups?.find { it.groupId in myUserGroupsIds }?.isAccepted == true
        }
        val itemInvites = creator.getItemInvites(itemGroups)
        val userGroupInvites = creator.getUserGroupInvites(userGroups)
        val userGroupsToDisplay = creator.getUserGroupsToDisplay(myUserGroups, myItemGroups)
        val usersToDisplay = creator.getUsersToDisplay(myItemGroups)
        if (isDataNotEmpty(usersToDisplay, userGroupsToDisplay, itemInvites, userGroupInvites)) {
            val data = SharingCenterViewModelContract.UIState.Data(
                itemInvites = itemInvites,
                userGroupInvites = userGroupInvites,
                users = usersToDisplay,
                userGroups = userGroupsToDisplay
            )
            uiStateData.tryEmit(data)
            uiState.tryEmit(data)
        } else {
            uiState.tryEmit(SharingCenterViewModelContract.UIState.Empty)
        }
    }

    private fun isDataNotEmpty(
        usersToDisplay: List<SharingContact.User>,
        userGroupsToDisplay: List<SharingContact.UserGroup>,
        itemInvites: List<SharingContact.ItemInvite>,
        userGroupInvites: List<SharingContact.UserGroupInvite>
    ) =
        usersToDisplay.isNotEmpty() || userGroupsToDisplay.isNotEmpty() || itemInvites.isNotEmpty() || userGroupInvites.isNotEmpty()

    override fun reloadData() {
        viewModelScope.run {
            launch { dataFlow.tryEmit(dataProvider.getItemGroups() to dataProvider.getUserGroups()) }
        }
    }

    override fun refresh() {
        dataSync.sync(Trigger.MANUAL)
    }

    override fun onClickNewShare(activity: Fragment) {
        proceedItemIfTeamspaceAllows(activity.requireActivity()) {
            navigator.goToNewShare(UsageLogCode80.From.SHARING_CENTER.code)
        }
    }

    private fun proceedItemIfTeamspaceAllows(
        activity: FragmentActivity,
        action: () -> Unit
    ) {
        teamspaceManager?.startFeatureOrNotify(
            activity,
            Teamspace.Feature.SHARING_DISABLED,
            object : FeatureCall {
                override fun startFeature() = action()
            }
        )
    }

    override fun onUserClicked(user: SharingContact.User) =
        navigator.goToItemsForUserFromPasswordSharing(user.name)

    override fun onUserGroupClicked(userGroup: SharingContact.UserGroup) =
        navigator.goToUserGroupFromPasswordSharing(
            userGroup.groupId,
            userGroup.name
        )

    override fun onStatusChanged(
        teamspace: Teamspace?,
        previousStatus: String?,
        newStatus: String?
    ) = Unit

    override fun onChange(teamspace: Teamspace?) =
        display(dataFlow.value.first, dataFlow.value.second)

    override fun onTeamspacesUpdate() = Unit

    override fun acceptItemGroup(invite: SharingContact.ItemInvite) {
        uiState.tryEmit(SharingCenterViewModelContract.UIState.RequestLoading)
        viewModelScope.launch {
            runCatching {
                dataProvider.acceptItemGroupInvite(invite.itemGroup, invite.item, true)
            }.onFailure {
                uiState.tryEmit(SharingCenterViewModelContract.UIState.RequestFailure)
            }.onSuccess {
                uiState.tryEmit(SharingCenterViewModelContract.UIState.RequestSuccess)
                reloadData()
            }
        }
    }

    override fun acceptUserGroup(invite: SharingContact.UserGroupInvite) {
        uiState.tryEmit(SharingCenterViewModelContract.UIState.RequestLoading)
        viewModelScope.launch {
            runCatching {
                dataProvider.acceptUserGroupInvite(invite.userGroup, true)
            }.onFailure {
                uiState.tryEmit(SharingCenterViewModelContract.UIState.RequestFailure)
            }.onSuccess {
                uiState.tryEmit(SharingCenterViewModelContract.UIState.RequestSuccess)
                reloadData()
            }
        }
    }
}
