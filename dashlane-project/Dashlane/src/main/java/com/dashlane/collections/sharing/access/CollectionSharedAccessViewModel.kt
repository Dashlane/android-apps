package com.dashlane.collections.sharing.access

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.collections.sharing.CollectionSharingViewState
import com.dashlane.collections.sharing.CollectionSharingViewState.ConfirmRevoke
import com.dashlane.collections.sharing.CollectionSharingViewState.Individual
import com.dashlane.collections.sharing.CollectionSharingViewState.Loading
import com.dashlane.collections.sharing.CollectionSharingViewState.MyselfRevoked
import com.dashlane.collections.sharing.CollectionSharingViewState.ShowList
import com.dashlane.collections.sharing.CollectionSharingViewState.UserGroup
import com.dashlane.collections.sharing.CollectionSharingViewState.ViewData
import com.dashlane.core.sharing.SharingItemUpdater
import com.dashlane.core.sharing.handleCollectionSharingResult
import com.dashlane.hermes.generated.definitions.Trigger
import com.dashlane.network.tools.authorization
import com.dashlane.server.api.Authorization
import com.dashlane.server.api.endpoints.sharinguserdevice.Collection
import com.dashlane.server.api.endpoints.sharinguserdevice.Permission
import com.dashlane.server.api.endpoints.sharinguserdevice.RevokeCollectionMembersService
import com.dashlane.server.api.pattern.UserIdFormat
import com.dashlane.server.api.pattern.UuidFormat
import com.dashlane.session.Session
import com.dashlane.session.SessionManager
import com.dashlane.sharing.model.isAccepted
import com.dashlane.sync.DataSync
import com.dashlane.ui.screens.fragments.userdata.sharing.center.SharingDataProvider
import com.dashlane.userfeatures.FeatureFlip
import com.dashlane.userfeatures.UserFeaturesChecker
import com.dashlane.util.inject.qualifiers.DefaultCoroutineDispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CollectionSharedAccessViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val sharingDataProvider: SharingDataProvider,
    private val sharingItemUpdater: SharingItemUpdater,
    private val revokeCollectionMembersService: RevokeCollectionMembersService,
    private val dataSync: DataSync,
    @DefaultCoroutineDispatcher
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val userFeaturesChecker: UserFeaturesChecker,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val session: Session
        get() = sessionManager.session!!
    val login: String
        get() = session.userId
    private val authorization: Authorization.User
        get() = session.authorization
    private val _uiState = MutableStateFlow<CollectionSharingViewState>(Loading())
    val uiState = _uiState.asStateFlow()
    private val navArgs = CollectionSharedAccessActivityArgs.fromSavedStateHandle(savedStateHandle)
    private val collectionId = navArgs.collectionId
    private val canShowRoles: Boolean
        get() = userFeaturesChecker.has(FeatureFlip.SHARING_COLLECTION_ROLES)

    init {
        loadGroupsAndIndividuals(collectionId)
    }

    private fun loadGroupsAndIndividuals(collectionId: String) {
        viewModelScope.launch(defaultDispatcher) {
            _uiState.emit(Loading())
            
            
            val collection = sharingDataProvider.getAcceptedCollections(needsAdminRights = false)
                .firstOrNull { it.uuid == collectionId }
            if (collection == null) {
                
                
                
                dataSync.sync(Trigger.SHARING)
                _uiState.emit(MyselfRevoked())
                return@launch
            }
            val userGroupsOfCollection =
                sharingDataProvider.getUserGroupsAccepted(login).filter { myGroup ->
                    collection.userGroups?.any { it.uuid == myGroup.groupId } == true
                }
            val userGroups = userGroupsOfCollection.map { userGroup ->
                UserGroup(
                    userGroup.groupId,
                    userGroup.name,
                    userGroup.users.size,
                    permission = collection.getGroupPermission(userGroup.groupId)
                )
            }.sortedBy { it.name }
            
            
            val individuals = collection.users?.map {
                Individual(
                    it.login,
                    accepted = it.isAccepted,
                    permission = it.permission
                )
            }?.sortedWith(compareBy({ it.accepted }, { it.username })) ?: emptyList()
            val isAdmin = sharingDataProvider.isCollectionShareAllowed(collection)
            val viewData = ViewData(
                userGroups,
                individuals,
                isAdmin = isAdmin,
                showRoles = canShowRoles
            )
            _uiState.emit(ShowList(viewData))
        }
    }

    fun onRevokeClicked(userGroup: UserGroup) = viewModelScope.launch {
        val data = _uiState.value.viewData
        _uiState.emit(ConfirmRevoke(data, groupToRevoke = userGroup))
    }

    fun onRevokeClicked(individual: Individual) = viewModelScope.launch {
        val data = _uiState.value.viewData
        _uiState.emit(ConfirmRevoke(data, userToRevoke = individual))
    }

    fun onRevokeCancelClicked() = viewModelScope.launch {
        _uiState.emit(ShowList(_uiState.value.viewData))
    }

    fun revokeMember(groupId: String? = null, userId: String? = null) {
        viewModelScope.launch {
            val collection = sharingDataProvider.getAcceptedCollections(needsAdminRights = true)
                .firstOrNull { it.uuid == collectionId }
            if ((groupId == null && userId == null) || collection == null) {
                _uiState.emit(ShowList(_uiState.value.viewData))
                return@launch
            }
            runCatching {
                revokeCollectionMembersService.execute(
                    userAuthorization = authorization,
                    request = RevokeCollectionMembersService.Request(
                        collectionId = UuidFormat(collection.uuid),
                        revision = collection.revision,
                        userGroupUUIDs = groupId?.let { listOf(UuidFormat(it)) },
                        userLogins = userId?.let { listOf(UserIdFormat(it)) }
                    )
                ).also {
                    it.data.collections?.let { collections ->
                        sharingItemUpdater.handleCollectionSharingResult(collections)
                    }
                }
            }.onFailure {
            }
            loadGroupsAndIndividuals(collectionId)
        }
    }

    fun onToggleSearch() {
        viewModelScope.launch {
            val originalViewData = _uiState.value.viewData
            val viewData = originalViewData.copy(showSearch = !originalViewData.showSearch)
            _uiState.emit(ShowList(viewData))
        }
    }

    private fun Collection.getGroupPermission(groupId: String) =
        userGroups?.find { it.uuid == groupId }?.permission ?: Permission.ADMIN
}
