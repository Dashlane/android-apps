package com.dashlane.sharingcenter

import android.content.res.Resources
import com.dashlane.design.iconography.IconTokens
import com.dashlane.frozenaccount.FrozenStateManager
import com.dashlane.hermes.generated.definitions.Trigger
import com.dashlane.server.api.endpoints.sharinguserdevice.Collection
import com.dashlane.server.api.endpoints.sharinguserdevice.ItemGroup
import com.dashlane.server.api.endpoints.sharinguserdevice.UserGroup
import com.dashlane.session.SessionManager
import com.dashlane.sharing.SharingDataProvider
import com.dashlane.sharing.model.getUser
import com.dashlane.sharing.model.isAccepted
import com.dashlane.sharing_center.R
import com.dashlane.sharingcenter.SharingCenterState.View.Loading
import com.dashlane.sharingcenter.contact.SharingContactCreator
import com.dashlane.sync.DataSync
import com.dashlane.teamspaces.ui.CurrentTeamSpaceUiFilter
import com.dashlane.vault.item.VaultListItemState
import com.dashlane.vault.item.VaultListItemStateFactory
import com.dashlane.vault.item.comparator.alphabeticComparator
import com.dashlane.vault.summary.SummaryObject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

fun load(
    currentTeamSpaceUiFilter: CurrentTeamSpaceUiFilter,
    sharingDataProvider: SharingDataProvider,
    sharingContactCreator: SharingContactCreator,
    vaultListItemStateFactory: VaultListItemStateFactory,
    sessionManager: SessionManager,
    resources: Resources,
    state: Loading,
): Flow<SharingCenterEvent> {
    return flow {
        currentTeamSpaceUiFilter.teamSpaceFilterState.collect {
            val itemGroups = sharingDataProvider.getItemGroups()
            val userGroups = sharingDataProvider.getUserGroups()
            val collections = sharingDataProvider.getCollections()

            val login = sessionManager.session?.username?.email ?: return@collect

            val (myUserGroups, myCollections, myItemGroups) = computeMySharedItems(
                login = login,
                itemGroups = itemGroups,
                userGroups = userGroups,
                collections = collections,
            )

            val userGroupsToDisplay =
                sharingContactCreator.getUserGroupsToDisplay(myUserGroups, myItemGroups, myCollections)
            val usersToDisplay = sharingContactCreator.getUsersToDisplay(myItemGroups, myCollections)

            val itemInvites = sharingContactCreator.getItemInvites(itemGroups)
            val userGroupInvites = sharingContactCreator.getUserGroupInvites(userGroups)
            val collectionInvites = sharingContactCreator.getCollectionInvites(collections)

            val mySharedSummaries = myItemGroups.mapNotNull {
                val itemId = it.items?.firstOrNull()?.itemId ?: return@mapNotNull null
                sharingDataProvider.getSummaryObject(itemId)
            }

            val peopleData = SharingCenterPeopleData(
                users = usersToDisplay,
                groups = userGroupsToDisplay,
            )
            val pendingRequestData = SharingCenterPendingRequestData(
                itemInvites = itemInvites.map { it to vaultListItemStateFactory.buildVaultListItemState(it.item) },
                groupInvites = userGroupInvites,
                collectionInvites = collectionInvites,
            )
            val contentData = computeContentData(
                myCollections = myCollections,
                mySharedSummaries = mySharedSummaries,
                myItemGroups = myItemGroups,
                sharingDataProvider = sharingDataProvider,
                vaultListItemStateFactory = vaultListItemStateFactory,
                resources = resources,
            )

            emit(
                SharingCenterEvent.Loaded(
                    peopleData = peopleData,
                    pendingRequestData = pendingRequestData,
                    contentData = contentData,
                ),
            )
        }
    }
}

fun computeMySharedItems(
    login: String,
    itemGroups: List<ItemGroup>,
    userGroups: List<UserGroup>,
    collections: List<Collection>,
): Triple<List<UserGroup>, List<Collection>, List<ItemGroup>> {
    val myUserGroups = userGroups.filter { it.users.find { it.userId == login }?.isAccepted == true }
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
    return Triple(myUserGroups, myCollections, myItemGroups)
}

fun processAcceptCollectionInvite(
    frozenStateManager: FrozenStateManager,
    sharingDataProvider: SharingDataProvider,
    dataSync: DataSync,
    resources: Resources,
    state: SharingCenterState.View.ProcessPendingInviteResponse.ProcessPendingCollectionInviteAccept,
) = flow {
    if (frozenStateManager.isAccountFrozen) {
        emit(SharingCenterEvent.FrozenAccountEvent)
        return@flow
    }
    sharingDataProvider.acceptCollectionInvite(
        collection = state.collectionInvite.collection,
        handleConflict = true,
    )
    val message = resources.getString(
        R.string.sharing_center_pending_invite_collection_accept_success,
        state.collectionInvite.collection.name,
    )
    emit(SharingCenterEvent.PendingRequestResponseSuccess(message))

    
    dataSync.sync(Trigger.MANUAL)
}.catch {
    emit(SharingCenterEvent.PendingRequestResponseFailure)
}

fun processDeclineCollectionInvite(
    sharingDataProvider: SharingDataProvider,
    resources: Resources,
    state: SharingCenterState.View.ProcessPendingInviteResponse.ProcessPendingCollectionInviteDecline,
) = flow<SharingCenterEvent> {
    sharingDataProvider.declineCollectionInvite(
        collection = state.collectionInvite.collection,
        handleConflict = true,
    )
    val message = resources.getString(
        R.string.sharing_center_pending_invite_collection_decline_success,
        state.collectionInvite.collection.name,
    )
    emit(SharingCenterEvent.PendingRequestResponseSuccess(message))
}.catch {
    emit(SharingCenterEvent.PendingRequestResponseFailure)
}

fun processAcceptGroupInvite(
    frozenStateManager: FrozenStateManager,
    sharingDataProvider: SharingDataProvider,
    dataSync: DataSync,
    resources: Resources,
    state: SharingCenterState.View.ProcessPendingInviteResponse.ProcessPendingGroupInviteAccept,
) = flow {
    if (frozenStateManager.isAccountFrozen) {
        emit(SharingCenterEvent.FrozenAccountEvent)
        return@flow
    }
    sharingDataProvider.acceptUserGroupInvite(
        userGroup = state.groupInvite.userGroup,
        handleConflict = true,
    )
    val message = resources.getString(R.string.sharing_center_pending_invite_group_accept_success, state.groupInvite.userGroup.name)
    emit(SharingCenterEvent.PendingRequestResponseSuccess(message))

    
    dataSync.sync(Trigger.MANUAL)
}.catch {
    emit(SharingCenterEvent.PendingRequestResponseFailure)
}

fun processDeclineGroupInvite(
    sharingDataProvider: SharingDataProvider,
    resources: Resources,
    state: SharingCenterState.View.ProcessPendingInviteResponse.ProcessPendingGroupInviteDecline,
) = flow<SharingCenterEvent> {
    sharingDataProvider.declineUserGroupInvite(
        userGroup = state.groupInvite.userGroup,
        handleConflict = true,
    )
    val message = resources.getString(R.string.sharing_center_pending_invite_group_decline_success, state.groupInvite.userGroup.name)
    emit(SharingCenterEvent.PendingRequestResponseSuccess(message))
}.catch {
    emit(SharingCenterEvent.PendingRequestResponseFailure)
}

fun processAcceptItemInvite(
    frozenStateManager: FrozenStateManager,
    sharingDataProvider: SharingDataProvider,
    dataSync: DataSync,
    resources: Resources,
    state: SharingCenterState.View.ProcessPendingInviteResponse.ProcessPendingItemInviteAccept,
) = flow {
    if (frozenStateManager.isAccountFrozen) {
        emit(SharingCenterEvent.FrozenAccountEvent)
        return@flow
    }
    sharingDataProvider.acceptItemGroupInvite(
        itemGroup = state.itemInvite.itemGroup,
        summaryObject = state.itemInvite.item,
        handleConflict = true,
    )
    val message = resources.getString(R.string.sharing_center_pending_invite_item_accept_success, state.title)
    emit(SharingCenterEvent.PendingRequestResponseSuccess(message))

    
    dataSync.sync(Trigger.MANUAL)
}.catch {
    emit(SharingCenterEvent.PendingRequestResponseFailure)
}

fun processDeclineItemInvite(
    sharingDataProvider: SharingDataProvider,
    resources: Resources,
    state: SharingCenterState.View.ProcessPendingInviteResponse.ProcessPendingItemInviteDecline,
) = flow<SharingCenterEvent> {
    sharingDataProvider.declineItemGroupInvite(
        itemGroup = state.itemInvite.itemGroup,
        summaryObject = state.itemInvite.item,
        handleConflict = true,
    )
    val message = resources.getString(R.string.sharing_center_pending_invite_item_decline_success, state.title)
    emit(SharingCenterEvent.PendingRequestResponseSuccess(message))
}.catch {
    emit(SharingCenterEvent.PendingRequestResponseFailure)
}

private suspend fun computeContentData(
    myCollections: List<Collection>,
    mySharedSummaries: List<SummaryObject>,
    myItemGroups: List<ItemGroup>,
    sharingDataProvider: SharingDataProvider,
    vaultListItemStateFactory: VaultListItemStateFactory,
    resources: Resources,
) =
    SharingCenterContentData(
        collections = myCollections.map {
            val itemCount = sharingDataProvider.getAcceptedCollectionsItems(
                collection = it,
                allItemGroups = myItemGroups,
            ).size
            VaultListItemState(
                id = it.uuid,
                title = it.name,
                description = resources.getQuantityString(
                    R.plurals.sharing_center_collection_item_count,
                    itemCount,
                    itemCount,
                ),
                isSharedIconVisible = true,
                thumbnail = VaultListItemState.ThumbnailState.Icon(
                    token = IconTokens.collectionOutlined,
                ),
            ) to itemCount
        }.sortedWith(
            compareBy<Pair<VaultListItemState, Int>, String?>(nullsLast(String.CASE_INSENSITIVE_ORDER)) {
                it.first.title
            }.thenByDescending { it.second },
        ).map { it.first },
        items = mySharedSummaries.map {
            SharedItemData(
                itemType = it.syncObjectType.xmlObjectName,
                vaultListItemState = vaultListItemStateFactory.buildVaultListItemState(it)
                    .copy(isSharedIconVisible = true),
            )
        }.sortedWith(
            comparator = compareBy(alphabeticComparator()) {
                it.vaultListItemState
            },
        ),
    )