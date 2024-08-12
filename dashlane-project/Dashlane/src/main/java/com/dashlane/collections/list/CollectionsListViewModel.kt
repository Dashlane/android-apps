package com.dashlane.collections.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.accountstatus.AccountStatusRepository
import com.dashlane.collections.businessSpaceData
import com.dashlane.collections.details.CollectionLimiter
import com.dashlane.collections.spaceData
import com.dashlane.server.api.endpoints.sharinguserdevice.Collection
import com.dashlane.session.SessionManager
import com.dashlane.storage.userdata.accessor.CollectionDataQuery
import com.dashlane.storage.userdata.accessor.DataSaver
import com.dashlane.storage.userdata.accessor.filter.CollectionFilter
import com.dashlane.teamspaces.manager.TeamSpaceAccessorProvider
import com.dashlane.teamspaces.model.TeamSpace
import com.dashlane.teamspaces.ui.CurrentTeamSpaceUiFilter
import com.dashlane.ui.screens.fragments.userdata.sharing.center.SharingDataProvider
import com.dashlane.vault.VaultActivityLogger
import com.dashlane.vault.model.SyncState
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.summary.toSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CollectionsListViewModel @Inject constructor(
    private val teamSpaceAccessorProvider: TeamSpaceAccessorProvider,
    private val accountStatusRepository: AccountStatusRepository,
    private val sessionManager: SessionManager,
    private val sharingDataProvider: SharingDataProvider,
    private val currentTeamSpaceUiFilter: CurrentTeamSpaceUiFilter,
    private val collectionLimiter: CollectionLimiter,
    private val collectionDataQuery: CollectionDataQuery,
    private val dataSaver: DataSaver,
    private val vaultActivityLogger: VaultActivityLogger
) : ViewModel() {
    private val _uiState = MutableStateFlow<ViewState>(ViewState.Loading(ViewData(emptyList())))
    val uiState = _uiState.asStateFlow()

    init {
        refreshCollections()
        viewModelScope.launch {
            dataSaver.savedItemFlow.collect {
                refreshCollections()
            }
        }
        viewModelScope.launch {
            sharingDataProvider.updatedItemFlow.collect {
                refreshCollections()
            }
        }
        
        viewModelScope.launch {
            currentTeamSpaceUiFilter.teamSpaceFilterState.collect {
                refreshCollections()
            }
        }

        
        viewModelScope.launch {
            accountStatusRepository.accountStatusState.collect { accountStatuses ->
                accountStatuses[sessionManager.session]?.let {
                    refreshCollections()
                }
            }
        }
    }

    private fun refreshCollections() {
        viewModelScope.launch {
            val collections = collectionDataQuery.queryAll(
                CollectionFilter().apply {
                    forCurrentSpace()
                }
            )
            val sharedCollections = sharingDataProvider.getAcceptedCollections(needsAdminRights = false)
            val collectionLimit = collectionLimiter.checkCollectionLimit(isShared = false)
            val allCollections =
                collections.mapNotNull { it.toCollectionViewData(collectionLimit) } + sharedCollections.mapNotNull {
                    val size = sharingDataProvider.getAcceptedCollectionsItems(it.uuid).size
                    it.toCollectionViewData(size, collectionLimit)
                }
            val viewData = ViewData(allCollections.sortedBy { it.name })
            if (viewData.collections.isNotEmpty()) {
                _uiState.emit(ViewState.List(viewData))
            } else {
                _uiState.emit(ViewState.Empty(viewData))
            }
        }
    }

    private fun SummaryObject.Collection.toCollectionViewData(userLimit: CollectionLimiter.UserLimit) =
        name?.let { name ->
            val spaceData = spaceData(teamSpaceAccessorProvider)
            val shareAllowedAndEnabled = spaceData?.businessSpace == true
            CollectionViewData(
                id = id,
                name = name,
                itemCount = vaultItems?.size ?: 0,
                spaceData = spaceData,
                shared = false,
                shareEnabled = shareAllowedAndEnabled,
                shareAllowed = shareAllowedAndEnabled,
                shareLimitedByTeam = userLimit == CollectionLimiter.UserLimit.NOT_ADMIN,
                editAllowed = true,
                deleteAllowed = true
            )
        }

    private suspend fun Collection.toCollectionViewData(size: Int, userLimit: CollectionLimiter.UserLimit): CollectionViewData? {
        if (currentTeamSpaceUiFilter.currentFilter.teamSpace == TeamSpace.Personal) {
            
            return null
        }
        val isAdmin = sharingDataProvider.isAdmin(this)
        return CollectionViewData(
            id = uuid,
            name = name,
            itemCount = size,
            spaceData = businessSpaceData(teamSpaceAccessorProvider),
            shared = true,
            shareEnabled = true,
            shareAllowed = isAdmin,
            shareLimitedByTeam = userLimit == CollectionLimiter.UserLimit.NOT_ADMIN,
            editAllowed = isAdmin,
            deleteAllowed = isAdmin
        )
    }

    fun deleteClicked(id: String, shared: Boolean) {
        viewModelScope.launch {
            if (shared) {
                deleteSharedCollection(collectionId = id)
                return@launch
            }
            val collection = collectionDataQuery.queryById(id)
            if (collection != null) {
                val collectionToDelete = collection.copy(syncState = SyncState.DELETED)
                dataSaver.save(collectionToDelete)
                vaultActivityLogger.sendCollectionDeletedActivityLog(collection = collection.toSummary())
            }
        }
    }

    fun dismissDialogClicked() {
        _uiState.update {
            ViewState.List(it.viewData.copy())
        }
    }

    private suspend fun deleteSharedCollection(collectionId: String) {
        val collections =
            sharingDataProvider.getAcceptedCollections(needsAdminRights = false).filter {
                it.uuid == collectionId
            }
        val collection = collections.firstOrNull() ?: return

        if (!sharingDataProvider.isDeleteAllowed(collection)) {
            _uiState.emit(ViewState.RevokeAccessPrompt(_uiState.value.viewData, collectionId))
            return
        }

        runCatching {
            sharingDataProvider.deleteCollection(collection, true)
        }.onSuccess {
            _uiState.update {
                ViewState.List(
                    viewData = it.viewData.copy(
                        collections = it.viewData.collections.filterNot { collectionViewData ->
                            collectionViewData.id == collectionId
                        }
                    )
                )
            }
        }.onFailure {
            _uiState.update {
                ViewState.SharedCollectionDeleteError(viewData = it.viewData)
            }
        }
    }
}