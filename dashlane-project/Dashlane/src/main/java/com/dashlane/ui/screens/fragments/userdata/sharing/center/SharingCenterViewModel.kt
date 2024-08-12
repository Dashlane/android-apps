package com.dashlane.ui.screens.fragments.userdata.sharing.center

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.accountstatus.AccountStatusRepository
import com.dashlane.events.AppEvents
import com.dashlane.events.SyncFinishedEvent
import com.dashlane.frozenaccount.FrozenStateManager
import com.dashlane.hermes.generated.definitions.Trigger
import com.dashlane.navigation.Navigator
import com.dashlane.navigation.paywall.PaywallIntroType
import com.dashlane.server.api.endpoints.sharinguserdevice.Collection
import com.dashlane.server.api.endpoints.sharinguserdevice.ItemGroup
import com.dashlane.server.api.endpoints.sharinguserdevice.UserGroup
import com.dashlane.session.Session
import com.dashlane.session.SessionManager
import com.dashlane.sharing.model.getUser
import com.dashlane.sharing.model.isAccepted
import com.dashlane.sync.DataSync
import com.dashlane.teamspaces.ui.Feature
import com.dashlane.teamspaces.ui.TeamSpaceRestrictionNotificator
import com.dashlane.vault.summary.SummaryObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SharingCenterViewModel @Inject constructor(
    private val dataProvider: SharingDataProvider,
    private val creator: SharingContactCreator,
    private val sessionManager: SessionManager,
    private val accountStatusRepository: AccountStatusRepository,
    private val navigator: Navigator,
    private val appEvents: AppEvents,
    private val dataSync: DataSync,
    private val restrictionNotificator: TeamSpaceRestrictionNotificator,
    private val frozenStateManager: FrozenStateManager,
) : ViewModel(), SharingCenterViewModelContract {
    private val session: Session?
        get() = sessionManager.session

    private val isAccountFrozen: Boolean
        get() = frozenStateManager.isAccountFrozen

    private val dataFlow = MutableStateFlow(Data())

    private val uiStateData = MutableStateFlow(SharingCenterViewModelContract.UIState.Data())

    override val uiState: MutableSharedFlow<SharingCenterViewModelContract.UIState> =
        MutableSharedFlow(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    init {
        
        viewModelScope.launch {
            accountStatusRepository.accountStatusState.collect { accountStatuses ->
                accountStatuses[session]?.let {
                    display(dataFlow.value.itemGroups, dataFlow.value.userGroups, dataFlow.value.collections)
                }
            }
        }
        appEvents.register(this, SyncFinishedEvent::class.java, false) {
            reloadData()
        }
        viewModelScope.launch {
            dataFlow.collect { data ->
                display(data.itemGroups, data.userGroups, data.collections)
            }
        }
        reloadData()
    }

    override fun onCleared() {
        super.onCleared()
        appEvents.unregister(this, SyncFinishedEvent::class.java)
    }

    override fun declineItemGroup(groupId: String) {
        val item = uiStateData.value.itemInvites.find { it.itemGroup.groupId == groupId } ?: return
        uiState.tryEmit(SharingCenterViewModelContract.UIState.RequestLoading)
        sendDeclineItemGroupRequest(itemGroup = item.itemGroup, summaryObject = item.item)
    }

    override fun declineCollection(collectionId: String) {
        val collection = uiStateData.value.collectionInvites.find {
            it.collection.uuid == collectionId
        }?.collection ?: return
        uiState.tryEmit(SharingCenterViewModelContract.UIState.RequestLoading)
        sendDeclineCollectionRequest(collection)
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
                uiState.tryEmit(SharingCenterViewModelContract.UIState.RequestSuccess())
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
                uiState.tryEmit(SharingCenterViewModelContract.UIState.RequestSuccess())
                reloadData()
            }
        }
    }

    private fun sendDeclineCollectionRequest(collection: Collection) {
        viewModelScope.launch {
            runCatching {
                dataProvider.declineCollectionInvite(collection, true)
            }.onFailure {
                uiState.tryEmit(SharingCenterViewModelContract.UIState.RequestFailure)
            }.onSuccess {
                uiState.tryEmit(SharingCenterViewModelContract.UIState.RequestSuccess())
                reloadData()
            }
        }
    }

    private fun display(
        itemGroups: List<ItemGroup>,
        userGroups: List<UserGroup>,
        collections: List<Collection>
    ) {
        val login = session?.username?.email ?: return
        val myUserGroups = userGroups.filter { it.getUser(login)?.isAccepted == true }
        val myUserGroupsIds = myUserGroups.map { it.groupId }
        val myCollections = collections.filter { collection ->
            collection.getUser(login)?.isAccepted == true || collection.userGroups?.find {
                it.uuid in myUserGroupsIds
            }?.isAccepted == true
        }
        val myItemGroups = itemGroups.filter { itemGroup ->
            itemGroup.collections?.any {
                myCollections.any { collection ->
                    collection.uuid == it.uuid && (
                        collection.getUser(login)?.isAccepted == true ||
                            collection.userGroups?.find { it.uuid in myUserGroupsIds }?.isAccepted == true
                        )
                }
            } == true || itemGroup.getUser(login)?.isAccepted == true ||
                itemGroup.groups?.find { it.groupId in myUserGroupsIds }?.isAccepted == true
        }
        val itemInvites = creator.getItemInvites(itemGroups)
        val userGroupInvites = creator.getUserGroupInvites(userGroups)
        val collectionInvites = creator.getCollectionInvites(collections)
        val userGroupsToDisplay =
            creator.getUserGroupsToDisplay(myUserGroups, myItemGroups, myCollections)
        val usersToDisplay = creator.getUsersToDisplay(myItemGroups, myCollections)
        val isDataNotEmpty = isDataNotEmpty(
            usersToDisplay,
            userGroupsToDisplay,
            itemInvites,
            userGroupInvites,
            collectionInvites
        )
        if (isDataNotEmpty) {
            val data = SharingCenterViewModelContract.UIState.Data(
                itemInvites = itemInvites,
                userGroupInvites = userGroupInvites,
                collectionInvites = collectionInvites,
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
        userGroupInvites: List<SharingContact.UserGroupInvite>,
        collectionInvites: List<SharingContact.CollectionInvite>
    ) = usersToDisplay.isNotEmpty() || userGroupsToDisplay.isNotEmpty() ||
        itemInvites.isNotEmpty() || userGroupInvites.isNotEmpty() || collectionInvites.isNotEmpty()

    override fun reloadData() {
        viewModelScope.run {
            launch {
                val data = Data(
                    dataProvider.getItemGroups(),
                    dataProvider.getUserGroups(),
                    dataProvider.getCollections()
                )
                dataFlow.tryEmit(data)
            }
        }
    }

    override fun refresh() {
        dataSync.sync(Trigger.MANUAL)
    }

    override fun onClickNewShare(activity: Fragment) {
        if (isAccountFrozen) {
            navigator.goToPaywall(PaywallIntroType.FROZEN_ACCOUNT)
            return
        }

        proceedItemIfTeamspaceAllows(activity.requireActivity()) {
            navigator.goToNewShare()
        }
    }

    private fun proceedItemIfTeamspaceAllows(
        activity: FragmentActivity,
        action: () -> Unit
    ) {
        restrictionNotificator.runOrNotifyTeamRestriction(activity, Feature.SHARING_DISABLED) {
            action()
        }
    }

    override fun onUserClicked(user: SharingContact.User) =
        navigator.goToItemsForUserFromPasswordSharing(user.name)

    override fun onUserGroupClicked(userGroup: SharingContact.UserGroup) =
        navigator.goToUserGroupFromPasswordSharing(
            userGroup.groupId,
            userGroup.name
        )

    override fun acceptItemGroup(invite: SharingContact.ItemInvite) {
        if (isAccountFrozen) {
            navigator.goToPaywall(PaywallIntroType.FROZEN_ACCOUNT)
            return
        }

        uiState.tryEmit(SharingCenterViewModelContract.UIState.RequestLoading)
        viewModelScope.launch {
            runCatching {
                dataProvider.acceptItemGroupInvite(invite.itemGroup, invite.item, true)
            }.onFailure {
                uiState.tryEmit(SharingCenterViewModelContract.UIState.RequestFailure)
            }.onSuccess {
                uiState.tryEmit(SharingCenterViewModelContract.UIState.RequestSuccess())
                reloadData()
            }
        }
    }

    override fun acceptUserGroup(invite: SharingContact.UserGroupInvite) {
        if (isAccountFrozen) {
            navigator.goToPaywall(PaywallIntroType.FROZEN_ACCOUNT)
            return
        }

        uiState.tryEmit(SharingCenterViewModelContract.UIState.RequestLoading)
        viewModelScope.launch {
            runCatching {
                dataProvider.acceptUserGroupInvite(invite.userGroup, true)
            }.onFailure {
                uiState.tryEmit(SharingCenterViewModelContract.UIState.RequestFailure)
            }.onSuccess {
                uiState.tryEmit(
                    SharingCenterViewModelContract.UIState.RequestSuccess(
                        invite.userGroup.name
                    )
                )
                reloadData()
                
                refresh()
            }
        }
    }

    override fun acceptCollection(invite: SharingContact.CollectionInvite) {
        if (isAccountFrozen) {
            navigator.goToPaywall(PaywallIntroType.FROZEN_ACCOUNT)
            return
        }

        uiState.tryEmit(SharingCenterViewModelContract.UIState.RequestLoading)
        viewModelScope.launch {
            runCatching {
                dataProvider.acceptCollectionInvite(invite.collection, true)
            }.onFailure {
                uiState.tryEmit(SharingCenterViewModelContract.UIState.RequestFailure)
            }.onSuccess {
                uiState.tryEmit(
                    SharingCenterViewModelContract.UIState.RequestSuccess(
                        invite.collection.name
                    )
                )
                reloadData()
                
                refresh()
            }
        }
    }

    data class Data(
        val itemGroups: List<ItemGroup> = emptyList(),
        val userGroups: List<UserGroup> = emptyList(),
        val collections: List<Collection> = emptyList()
    )
}
