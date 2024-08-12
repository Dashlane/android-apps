package com.dashlane.collections.sharing.share

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.collections.details.CollectionLimiter
import com.dashlane.collections.sharing.CollectionSharingViewState
import com.dashlane.collections.sharing.CollectionSharingViewState.Individual
import com.dashlane.collections.sharing.CollectionSharingViewState.Loading
import com.dashlane.collections.sharing.CollectionSharingViewState.SharingFailed
import com.dashlane.collections.sharing.CollectionSharingViewState.SharingRestricted
import com.dashlane.collections.sharing.CollectionSharingViewState.SharingSuccess
import com.dashlane.collections.sharing.CollectionSharingViewState.ShowList
import com.dashlane.collections.sharing.CollectionSharingViewState.UserGroup
import com.dashlane.collections.sharing.CollectionSharingViewState.ViewData
import com.dashlane.collections.sharing.item.CollectionSharingItemDataProvider
import com.dashlane.core.sharing.SharingItemUpdater
import com.dashlane.core.sharing.handleCollectionSharingResult
import com.dashlane.network.tools.authorization
import com.dashlane.preference.UserPreferencesManager
import com.dashlane.server.api.Authorization
import com.dashlane.server.api.endpoints.premium.PremiumStatus.PremiumCapability.Capability
import com.dashlane.server.api.endpoints.sharinguserdevice.Collection
import com.dashlane.server.api.endpoints.sharinguserdevice.CreateCollectionService
import com.dashlane.server.api.endpoints.sharinguserdevice.InviteCollectionMembersService
import com.dashlane.server.api.endpoints.sharinguserdevice.Permission
import com.dashlane.session.Session
import com.dashlane.session.SessionManager
import com.dashlane.sharing.internal.builder.request.SharingRequestRepository
import com.dashlane.sharing.internal.model.GroupToInvite
import com.dashlane.sharing.internal.model.UserToInvite
import com.dashlane.sharing.service.FindUsersDataProvider
import com.dashlane.storage.userdata.accessor.CollectionDataQuery
import com.dashlane.storage.userdata.accessor.DataSaver
import com.dashlane.teamspaces.manager.TeamSpaceAccessor
import com.dashlane.ui.screens.fragments.userdata.sharing.center.SharingDataProvider
import com.dashlane.ui.screens.fragments.userdata.sharing.center.SharingDataUpdateProvider
import com.dashlane.featureflipping.UserFeaturesChecker
import com.dashlane.util.inject.OptionalProvider
import com.dashlane.utils.coroutines.inject.qualifiers.DefaultCoroutineDispatcher
import com.dashlane.vault.model.SyncState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@Suppress("LargeClass")
@HiltViewModel
class CollectionsNewShareViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val sharingDataProvider: SharingDataProvider,
    private val sharingDataUpdateProvider: SharingDataUpdateProvider,
    private val sharingRequestRepository: SharingRequestRepository,
    private val createCollectionService: CreateCollectionService,
    private val inviteCollectionMembersService: InviteCollectionMembersService,
    private val findUsersDataProvider: FindUsersDataProvider,
    private val teamSpaceAccessorProvider: OptionalProvider<TeamSpaceAccessor>,
    private val sharingItemUpdater: SharingItemUpdater,
    private val userPreferencesManager: UserPreferencesManager,
    private val userFeaturesChecker: UserFeaturesChecker,
    private val collectionSharingItemDataProvider: CollectionSharingItemDataProvider,
    @DefaultCoroutineDispatcher
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val collectionDataQuery: CollectionDataQuery,
    private val dataSaver: DataSaver,
    private val collectionLimiter: CollectionLimiter,
    savedStateHandle: SavedStateHandle
) : ViewModel(), UserInteractionListener {
    private val session: Session
        get() = sessionManager.session!!
    private val login: String
        get() = session.userId
    private val authorization: Authorization.User
        get() = session.authorization

    private val _uiState = MutableStateFlow<CollectionSharingViewState>(Loading())
    val uiState = _uiState.asStateFlow()
    private val navArgs = CollectionNewShareActivityArgs.fromSavedStateHandle(savedStateHandle)
    private val collectionId = navArgs.collectionId
    private val selectedGroups = mutableListOf<String>()
    private val selectedIndividuals = mutableListOf<String>()
    private val showSharingButton: Boolean
        get() = selectedGroups.isNotEmpty() || selectedIndividuals.isNotEmpty()

    init {
        loadGroupsAndIndividuals(collectionId)
    }

    private fun loadGroupsAndIndividuals(collectionId: String) {
        viewModelScope.launch(defaultDispatcher) {
            val isSharingAllowedByTeam: Boolean =
                teamSpaceAccessorProvider.get()?.isSharingDisabled == false &&
                    userFeaturesChecker.has(Capability.COLLECTIONSHARING)
            if (!isSharingAllowedByTeam) {
                _uiState.emit(SharingRestricted())
                return@launch
            }
            val myUserGroups = sharingDataProvider.getUserGroupsAccepted(login).map { userGroup ->
                UserGroup(
                    userGroup.groupId,
                    userGroup.name,
                    userGroup.users.size,
                    selected = selectedGroups.contains(userGroup.groupId)
                )
            }.sortedBy { it.name }
            val individuals = sharingDataProvider.getTeamLogins()
                
                .filter { username -> username != login }
                .map { username ->
                    Individual(username, selected = selectedIndividuals.contains(username))
                }
                .sortedBy { it.username }

            val viewData = ViewData(
                myUserGroups,
                individuals,
                collectionName = getCollectionName(collectionId),
                showSharingButton = showSharingButton,
                showSharingLimit = getCollectionLimit()
            )
            _uiState.emit(ShowList(viewData))
        }
    }

    private fun refreshGroupsAndIndividuals() = viewModelScope.launch {
        if (uiState.value !is ShowList) return@launch
        val originalViewData = uiState.value.viewData
        
        val viewData = originalViewData.copy(
            userGroups = originalViewData.userGroups.map { userGroup ->
                userGroup.copy(selected = selectedGroups.contains(userGroup.groupId))
            },
            individuals = originalViewData.individuals.map { individual ->
                individual.copy(selected = selectedIndividuals.contains(individual.username))
            },
            showSharingButton = showSharingButton,
            showSharingLimit = getCollectionLimit()
        )
        _uiState.emit(ShowList(viewData))
    }

    override fun onGroupSelectionChange(group: UserGroup) {
        if (selectedGroups.contains(group.groupId)) {
            selectedGroups.remove(group.groupId)
        } else {
            selectedGroups.add(group.groupId)
        }
        refreshGroupsAndIndividuals()
    }

    override fun onIndividualSelectionChange(individual: Individual) {
        if (selectedIndividuals.contains(individual.username)) {
            selectedIndividuals.remove(individual.username)
        } else {
            selectedIndividuals.add(individual.username)
        }
        refreshGroupsAndIndividuals()
    }

    override fun onShareClicked() {
        viewModelScope.launch(defaultDispatcher) {
            _uiState.emit(Loading(viewData = _uiState.value.viewData.copy()))
            val sharedCollection = doSharing(collectionId, selectedGroups, selectedIndividuals)
            if (sharedCollection != null) {
                
                deletePersonalCollection(collectionId)
                _uiState.emit(
                    SharingSuccess(
                        viewData = _uiState.value.viewData.copy(
                            collectionName = getCollectionName(collectionId),
                            sharedCollectionId = sharedCollection.uuid
                        )
                    )
                )
            } else {
                _uiState.emit(
                    SharingFailed(
                        viewData = _uiState.value.viewData.copy(
                            collectionName = getCollectionName(collectionId)
                        )
                    )
                )
            }
        }
    }

    override fun onToggleSearch() {
        viewModelScope.launch {
            val originalViewData = _uiState.value.viewData
            val viewData = originalViewData.copy(showSearch = !originalViewData.showSearch)
            _uiState.emit(ShowList(viewData))
        }
    }

    @Suppress("LongMethod")
    private suspend fun doSharing(
        collectionId: String,
        groups: MutableList<String>,
        individuals: MutableList<String>
    ): Collection? {
        
        val businessSpaceId = teamSpaceAccessorProvider.get()?.currentBusinessTeam?.teamId
        if (businessSpaceId == null) {
            error("User has no business space to share Collections to")
            return null
        }
        
        val usersFound = findUsersDataProvider.findUsers(session, individuals)
        val usersToInvite = individuals.map { individual ->
            val userFound = usersFound.find {
                it.email == individual || it.login == individual
            }
            UserToInvite(
                userId = userFound?.login ?: "",
                alias = userFound?.email ?: individual,
                permission = Permission.ADMIN, 
                publicKey = userFound?.publicKey
            )
        }
        val userGroups = sharingDataProvider.getUserGroupsAccepted(login)
        val groupsToInvite = groups.mapNotNull { group ->
            val foundGroup = userGroups.find { it.groupId == group } ?: return@mapNotNull null
            GroupToInvite(
                foundGroup,
                Permission.ADMIN
            )
        }
        
        
        val collection =
            sharingDataProvider.getAcceptedCollections(needsAdminRights = false)
                .firstOrNull { it.uuid == collectionId }
        if (collection == null) {
            verbose("Shared collection does not exist and will be created")
            
            val sharedCollection =
                createCollection(collectionId, usersToInvite, groupsToInvite, businessSpaceId)
                    ?: return null
            
            
            val myItemGroups = sharingDataProvider.getItemGroups()
            val collectionItems = collectionDataQuery.queryVaultItemsWithCollectionId(collectionId)

            val (existingItemGroups, itemGroupsToCreate) = collectionItems.map { item ->
                item to collectionSharingItemDataProvider.findItemGroup(myItemGroups, item.id)
            }.partition { it.second != null }

            val createdItemGroups =
                collectionSharingItemDataProvider.createMultipleItemGroups(
                    itemList = itemGroupsToCreate.map { it.first }
                )

            val itemGroups = existingItemGroups.mapNotNull { it.second } + createdItemGroups

            
            
            if (collectionItems.size != itemGroups.size) {
                sharingItemUpdater.handleCollectionSharingResult(updatedItemGroups = createdItemGroups)
                error(
                    "Some ItemGroups can't be created or found," +
                        "${collectionItems.size} item(s) in $collectionId but " +
                        "only ${itemGroups.size} ItemGroup(s) available"
                )
                return null
            }
            
            if (itemGroups.isEmpty()) {
                return sharedCollection.also {
                    sharingItemUpdater.handleCollectionSharingResult(listOf(it))
                }
            }
            
            val collectionsAndGroups =
                collectionSharingItemDataProvider.addItemsGroupsToCollection(
                    sharedCollection,
                    itemGroups,
                    userGroups
                )
            if (collectionsAndGroups == null) {
                
                
                val items = sharingDataUpdateProvider.getUpdatedItemGroups(createdItemGroups)
                    ?: createdItemGroups
                sharingItemUpdater.handleCollectionSharingResult(updatedItemGroups = items)
                return null
            }
            val updatedCollections = collectionsAndGroups.first
            return updatedCollections.first().also {
                sharingItemUpdater.handleCollectionSharingResult(
                    collectionsAndGroups.first,
                    collectionsAndGroups.second
                )
            }
        } else {
            return inviteToCollection(collection, userGroups, usersToInvite, groupsToInvite)?.also {
                sharingItemUpdater.handleCollectionSharingResult(it)
            }?.first()
        }
    }

    private suspend fun createCollection(
        collectionId: String,
        usersToInvite: List<UserToInvite>,
        groupsToInvite: List<GroupToInvite>,
        spaceId: String
    ): Collection? {
        
        val author = UserToInvite(
            userId = login,
            alias = session.username.email,
            permission = Permission.ADMIN,
            publicKey = userPreferencesManager.publicKey
        )

        
        val response = runCatching {
            createCollectionService.execute(
                authorization,
                sharingRequestRepository.createCollectionRequest(
                    collectionName = getCollectionName(collectionId) ?: "",
                    author = author,
                    teamId = spaceId,
                    users = usersToInvite,
                    groups = groupsToInvite
                )
            )
        }.onFailure {
            error(
                "Collection: $collectionId can't be created for space: $spaceId",
                throwable = it
            )
        }
        return response.getOrNull()?.data?.collections?.first()
    }

    private suspend fun inviteToCollection(
        collection: Collection,
        myUserGroups: List<com.dashlane.server.api.endpoints.sharinguserdevice.UserGroup>,
        usersToInvite: List<UserToInvite>,
        groupsToInvite: List<GroupToInvite>
    ): List<Collection>? {
        
        val response = runCatching {
            inviteCollectionMembersService.execute(
                authorization,
                sharingRequestRepository.createInviteCollectionMembersRequest(
                    collection = collection,
                    myUserGroups = myUserGroups,
                    users = usersToInvite,
                    groups = groupsToInvite
                )
            )
        }
        return response.onFailure {
            error("Users can't be invited to Collection", throwable = it)
        }.getOrNull()?.data?.collections
    }

    private suspend fun deletePersonalCollection(collectionId: String) {
        collectionDataQuery.queryById(collectionId)?.let {
            dataSaver.save(it.copy(syncState = SyncState.DELETED))
        }
    }

    private suspend fun getCollectionName(collectionId: String) =
        sharingDataProvider.getAcceptedCollections(needsAdminRights = false)
            .firstOrNull { it.uuid == collectionId }?.name
            ?: collectionDataQuery.queryById(collectionId)?.syncObject?.name

    private suspend fun getCollectionLimit() =
        showSharingButton && collectionLimiter.checkCollectionLimit() == CollectionLimiter.UserLimit.APPROACHING_LIMIT
}

internal interface UserInteractionListener {
    fun onGroupSelectionChange(group: UserGroup)
    fun onIndividualSelectionChange(individual: Individual)
    fun onShareClicked()
    fun onToggleSearch()
}
