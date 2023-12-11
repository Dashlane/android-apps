package com.dashlane.collections.sharing

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.collections.sharing.NewCollectionShareViewState.Individual
import com.dashlane.collections.sharing.NewCollectionShareViewState.Loading
import com.dashlane.collections.sharing.NewCollectionShareViewState.SharingFailed
import com.dashlane.collections.sharing.NewCollectionShareViewState.SharingSuccess
import com.dashlane.collections.sharing.NewCollectionShareViewState.UserGroup
import com.dashlane.collections.sharing.NewCollectionShareViewState.ViewData
import com.dashlane.core.sharing.SharingDao
import com.dashlane.core.sharing.SharingItemUpdater
import com.dashlane.core.sharing.handleCollectionSharingResult
import com.dashlane.core.sharing.toItemForEmailing
import com.dashlane.core.sharing.toSharedVaultItemLite
import com.dashlane.core.xmlconverter.DataIdentifierSharingXmlConverter
import com.dashlane.network.tools.authorization
import com.dashlane.preference.UserPreferencesManager
import com.dashlane.server.api.Authorization
import com.dashlane.server.api.endpoints.sharinguserdevice.AddItemGroupsToCollectionService
import com.dashlane.server.api.endpoints.sharinguserdevice.Collection
import com.dashlane.server.api.endpoints.sharinguserdevice.CreateCollectionService
import com.dashlane.server.api.endpoints.sharinguserdevice.CreateItemGroupService
import com.dashlane.server.api.endpoints.sharinguserdevice.InviteCollectionMembersService
import com.dashlane.server.api.endpoints.sharinguserdevice.ItemGroup
import com.dashlane.server.api.endpoints.sharinguserdevice.ItemUpload
import com.dashlane.server.api.endpoints.sharinguserdevice.Permission
import com.dashlane.session.Session
import com.dashlane.session.SessionManager
import com.dashlane.sharing.internal.builder.request.SharingRequestRepository
import com.dashlane.sharing.internal.model.GroupToInvite
import com.dashlane.sharing.internal.model.ItemToShare
import com.dashlane.sharing.internal.model.UserToInvite
import com.dashlane.sharing.service.FindUsersDataProvider
import com.dashlane.storage.DataStorageProvider
import com.dashlane.storage.userdata.accessor.MainDataAccessor
import com.dashlane.teamspaces.CombinedTeamspace
import com.dashlane.teamspaces.PersonalTeamspace
import com.dashlane.teamspaces.manager.TeamspaceAccessorProvider
import com.dashlane.ui.screens.fragments.userdata.sharing.center.SharingDataProvider
import com.dashlane.ui.screens.fragments.userdata.sharing.center.SharingDataUpdateProvider
import com.dashlane.util.inject.qualifiers.DefaultCoroutineDispatcher
import com.dashlane.util.userfeatures.UserFeaturesChecker
import com.dashlane.vault.model.SyncState
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.xml.domain.SyncObjectType
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
    private val dataStorageProvider: DataStorageProvider,
    private val sharingDataProvider: SharingDataProvider,
    private val sharingRequestRepository: SharingRequestRepository,
    private val createCollectionService: CreateCollectionService,
    private val addItemGroupsToCollectionService: AddItemGroupsToCollectionService,
    private val createItemGroupService: CreateItemGroupService,
    private val inviteCollectionMembersService: InviteCollectionMembersService,
    private val findUsersDataProvider: FindUsersDataProvider,
    private val teamspaceAccessor: TeamspaceAccessorProvider,
    private val sharingItemUpdater: SharingItemUpdater,
    private val userPreferencesManager: UserPreferencesManager,
    private val xmlConverter: DataIdentifierSharingXmlConverter,
    private val sharingDataUpdateProvider: SharingDataUpdateProvider,
    private val userFeaturesChecker: UserFeaturesChecker,
    @DefaultCoroutineDispatcher
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default,
    mainDataAccessor: MainDataAccessor,
    savedStateHandle: SavedStateHandle
) : ViewModel(), UserInteractionListener {
    private val session: Session
        get() = sessionManager.session!!
    private val login: String
        get() = session.userId
    private val authorization: Authorization.User
        get() = session.authorization
    private val sharingDao: SharingDao
        get() = dataStorageProvider.sharingDao
    private val collectionDataQuery = mainDataAccessor.getCollectionDataQuery()
    private val dataSaver = mainDataAccessor.getDataSaver()
    private val _uiState = MutableStateFlow<NewCollectionShareViewState>(Loading())
    val uiState = _uiState.asStateFlow()
    private val navArgs = CollectionNewShareActivityArgs.fromSavedStateHandle(savedStateHandle)
    private val collectionId = navArgs.collectionId
    private val selectedGroups = mutableListOf<String>()
    private val selectedIndividuals = mutableListOf<String>()
    private val showSharingButton: Boolean
        get() = selectedGroups.isNotEmpty() || selectedIndividuals.isNotEmpty()
    private val shareEnabled: Boolean
        get() = userFeaturesChecker.has(UserFeaturesChecker.FeatureFlip.SHARING_COLLECTION_MILESTONE_1)

    init {
        loadGroupsAndIndividuals(collectionId, shareEnabled)
    }

    private fun loadGroupsAndIndividuals(collectionId: String, shareEnabled: Boolean) {
        viewModelScope.launch(defaultDispatcher) {
            if (!shareEnabled) {
                _uiState.emit(SharingFailed())
                return@launch
            }
            val myUserGroups = sharingDao.loadUserGroupsAccepted(login)?.map { userGroup ->
                UserGroup(
                    userGroup.groupId,
                    userGroup.name,
                    userGroup.users.size,
                    selectedGroups.contains(userGroup.groupId)
                )
            } ?: emptyList()
            val individuals = sharingDataProvider.getTeamLogins()
                
                .filter { username -> username != login }
                .map { username -> Individual(username, selectedIndividuals.contains(username)) }
            val viewData = ViewData(
                myUserGroups,
                individuals,
                collectionName = getCollectionName(collectionId),
                showSharingButton = showSharingButton
            )
            _uiState.emit(NewCollectionShareViewState.List(viewData))
        }
    }

    private fun refreshGroupsAndIndividuals() = viewModelScope.launch {
        if (uiState.value !is NewCollectionShareViewState.List) return@launch
        val originalViewData = uiState.value.viewData
        
        val viewData = originalViewData.copy(
            userGroups = originalViewData.userGroups.map { userGroup ->
                userGroup.copy(selected = selectedGroups.contains(userGroup.groupId))
            },
            individuals = originalViewData.individuals.map { individual ->
                individual.copy(selected = selectedIndividuals.contains(individual.username))
            },
            showSharingButton = showSharingButton
        )
        _uiState.emit(NewCollectionShareViewState.List(viewData))
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
            _uiState.emit(Loading())
            val sharedCollection = doSharing(collectionId, selectedGroups, selectedIndividuals)
            if (sharedCollection != null) {
                
                deletePersonalCollection(collectionId)
                _uiState.emit(
                    SharingSuccess(
                        ViewData(
                            collectionName = getCollectionName(collectionId),
                            sharedCollectionId = sharedCollection.uuid
                        )
                    )
                )
            } else {
                _uiState.emit(
                    SharingFailed(
                        ViewData(
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
            _uiState.emit(NewCollectionShareViewState.List(viewData))
        }
    }

    @Suppress("LongMethod")
    private suspend fun doSharing(
        collectionId: String,
        groups: MutableList<String>,
        individuals: MutableList<String>
    ): Collection? {
        
        val businessSpaceId = teamspaceAccessor.get()?.all
            ?.minus(setOf(CombinedTeamspace, PersonalTeamspace))
            ?.first()
            ?.teamId
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
        val userGroups = sharingDao.loadUserGroupsAccepted(login)
        val groupsToInvite = groups.mapNotNull { group ->
            val foundGroup = userGroups?.find { it.groupId == group } ?: return@mapNotNull null
            GroupToInvite(
                foundGroup,
                Permission.ADMIN
            )
        }
        
        
        val collection =
            sharingDataProvider.getAcceptedCollections().firstOrNull { it.uuid == collectionId }
        if (collection == null) {
            verbose("Shared collection does not exist and will be created")
            
            val sharedCollection =
                createCollection(collectionId, usersToInvite, groupsToInvite, businessSpaceId)
                    ?: return null
            
            
            val myItemGroups = sharingDataProvider.getItemGroups()
            val collectionItems = collectionDataQuery.queryVaultItemsWithCollectionId(collectionId)
            val itemGroups = collectionItems.mapNotNull { item ->
                findItemGroup(myItemGroups, item).also { foundGroup ->
                    if (foundGroup == null) {
                        verbose("No existing ItemGroup found for ${item.anonymousId}")
                    }
                } ?: createItemGroup(item)
            }
            
            
            if (collectionItems.size != itemGroups.size) {
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
            
            val collectionsAndGroups = addItemsGroupsToCollection(sharedCollection, itemGroups)
            val updatedCollections = collectionsAndGroups?.first
            return updatedCollections?.first()?.also {
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

    private suspend fun addItemsGroupsToCollection(
        sharedCollection: Collection,
        itemGroups: List<ItemGroup>
    ): Pair<List<Collection>, List<ItemGroup>>? {
        return runCatching {
            val request = sharingRequestRepository.createAddItemGroupsToCollectionRequest(
                collection = sharedCollection,
                itemGroups = itemGroups
            )
            addItemGroupsToCollectionService.execute(authorization, request).let {
                sharingDataUpdateProvider.getUpdatedItemGroups(itemGroups).let { updatedGroups ->
                    if (updatedGroups == null) {
                        error("Failed to retrieve linked ItemGroup(s) from server")
                    }
                    it.data.collections!! to updatedGroups!!
                }
            }
        }.onFailure {
            error("Can't link ItemGroups with Collection", throwable = it)
        }.getOrNull()
    }

    private fun findItemGroup(itemGroups: List<ItemGroup>, item: SummaryObject) =
        itemGroups.firstOrNull { itemGroup -> itemGroup.items?.first()?.itemId == item.id }

    private suspend fun createItemGroup(item: SummaryObject): ItemGroup? {
        
        if (item.syncObjectType != SyncObjectType.AUTHENTIFIANT) return null
        val dataIdentifierWithExtraData =
            sharingDao.getItemWithExtraData(item.id, item.syncObjectType)
        return runCatching {
            val request = sharingRequestRepository.createItemGroupRequest(
                users = emptyList(), 
                groups = emptyList(),
                item = ItemToShare(
                    item.id,
                    xmlConverter.toXml(dataIdentifierWithExtraData)!!,
                    ItemUpload.ItemType.AUTHENTIFIANT
                ),
                itemForEmailing = item.toSharedVaultItemLite().toItemForEmailing(),
                auditLogs = null
            )
            createItemGroupService.execute(authorization, request)
            
            
        }.onFailure {
            error("ItemGroup can't be created for ${item.anonymousId}", throwable = it)
        }.getOrNull()?.data?.itemGroups?.first()
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
        myUserGroups: List<com.dashlane.server.api.endpoints.sharinguserdevice.UserGroup>?,
        usersToInvite: List<UserToInvite>,
        groupsToInvite: List<GroupToInvite>
    ): List<Collection>? {
        
        val response = runCatching {
            inviteCollectionMembersService.execute(
                authorization,
                sharingRequestRepository.createInviteCollectionMembersRequest(
                    collection = collection,
                    myUserGroups = myUserGroups ?: emptyList(),
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
        sharingDataProvider.getAcceptedCollections()
            .firstOrNull { it.uuid == collectionId }?.name
            ?: collectionDataQuery.queryById(collectionId)?.syncObject?.name
}

internal interface UserInteractionListener {
    fun onGroupSelectionChange(group: UserGroup)
    fun onIndividualSelectionChange(individual: Individual)
    fun onShareClicked()
    fun onToggleSearch()
}
