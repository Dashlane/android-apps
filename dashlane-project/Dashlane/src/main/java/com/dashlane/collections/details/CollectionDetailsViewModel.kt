package com.dashlane.collections.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.collections.businessSpaceData
import com.dashlane.collections.sharing.item.CollectionSharingItemDataProvider
import com.dashlane.collections.spaceData
import com.dashlane.storage.userdata.accessor.CollectionDataQuery
import com.dashlane.storage.userdata.accessor.CredentialDataQuery
import com.dashlane.storage.userdata.accessor.DataSaver
import com.dashlane.storage.userdata.accessor.queryFirst
import com.dashlane.teamspaces.manager.TeamSpaceAccessorProvider
import com.dashlane.ui.screens.fragments.userdata.sharing.center.SharingDataProvider
import com.dashlane.url.toUrlDomainOrNull
import com.dashlane.userfeatures.FeatureFlip
import com.dashlane.userfeatures.UserFeaturesChecker
import com.dashlane.util.inject.qualifiers.DefaultCoroutineDispatcher
import com.dashlane.vault.VaultActivityLogger
import com.dashlane.vault.model.SyncState
import com.dashlane.vault.model.copySyncObject
import com.dashlane.vault.model.loginForUi
import com.dashlane.vault.model.titleForListNormalized
import com.dashlane.vault.model.urlDomain
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.summary.toSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CollectionDetailsViewModel @Inject constructor(
    private val teamSpaceAccessorProvider: TeamSpaceAccessorProvider,
    @DefaultCoroutineDispatcher private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default,
    savedStateHandle: SavedStateHandle,
    private val collectionDataQuery: CollectionDataQuery,
    private val credentialDataQuery: CredentialDataQuery,
    private val dataSaver: DataSaver,
    private val sharingDataProvider: SharingDataProvider,
    private val collectionSharingItemDataProvider: CollectionSharingItemDataProvider,
    private val userFeaturesChecker: UserFeaturesChecker,
    private val collectionLimiter: CollectionLimiter,
    private val vaultActivityLogger: VaultActivityLogger
) : ViewModel() {
    private val shareEnabled: Boolean
        get() = userFeaturesChecker.has(FeatureFlip.SHARING_COLLECTION)
    private val allowAllRoles: Boolean
        get() = userFeaturesChecker.has(FeatureFlip.SHARING_COLLECTION_ROLES)

    
    val isCollectionSharingLimited: Boolean
        get() = uiState.value.viewData.collectionLimit == CollectionLimiter.UserLimit.REACHED_LIMIT

    val navArgs = CollectionDetailsFragmentArgs.fromSavedStateHandle(savedStateHandle)
    var listeningChanges = true
    var userAccessCouldChange = false

    private val _uiState = MutableStateFlow<ViewState>(
        ViewState.Loading(
            ViewData(
                collectionName = null,
                shared = false,
                items = emptyList(),
                spaceData = null,
                canRemoveFromSharedCollection = shareEnabled
            )
        )
    )
    val uiState = _uiState.asStateFlow()

    init {
        refreshCollectionDetails()
        viewModelScope.launch {
            dataSaver.savedItemFlow.collect {
                
                
                if (listeningChanges) refreshCollectionDetails()
            }
        }
    }

    fun mayRefresh() {
        if (userAccessCouldChange) {
            refreshCollectionDetails()
            userAccessCouldChange = false
        }
    }

    private fun refreshCollectionDetails() {
        viewModelScope.launch(defaultDispatcher) {
            val sharedCollection = navArgs.sharedCollection
            if (sharedCollection && shareEnabled) {
                refreshSharedCollection()
            } else {
                refreshPrivateCollection()
            }
        }
    }

    private suspend fun refreshSharedCollection() {
        val collections =
            sharingDataProvider.getAcceptedCollections(needsAdminRights = !allowAllRoles).filter {
                it.uuid == navArgs.collectionId
            }
        if (collections.isEmpty()) {
            _uiState.emit(ViewState.Deleted(_uiState.value.viewData))
            return
        }
        val collection = collections.first()
        val title = collection.name
        val items = sharingDataProvider.getAcceptedCollectionsItems(navArgs.collectionId)
        val collectionLimit = collectionLimiter.checkCollectionLimit(navArgs.sharedCollection)
        val viewData = ViewData(
            collectionName = title,
            shared = true,
            items = items.mapNotNull { it.toSummaryForUi() },
            spaceData = businessSpaceData(teamSpaceAccessorProvider),
            canRemoveFromSharedCollection = shareEnabled,
            collectionLimit = collectionLimit
        )

        if (items.isNotEmpty()) {
            _uiState.emit(ViewState.List(viewData))
        } else {
            _uiState.emit(ViewState.Empty(viewData))
        }
    }

    private suspend fun refreshPrivateCollection() {
        val collection = collectionDataQuery.queryById(navArgs.collectionId)
        if (collection == null) {
            _uiState.emit(ViewState.Deleted(_uiState.value.viewData))
            return
        }

        val title = collection.syncObject.name
        val items = collectionDataQuery.queryVaultItemsWithCollectionId(navArgs.collectionId)
        val collectionLimit = collectionLimiter.checkCollectionLimit(navArgs.sharedCollection)

        val viewData = ViewData(
            collectionName = title,
            shared = false,
            items = items.mapNotNull { it.toSummaryForUi() },
            spaceData = collection.syncObject.spaceData(teamSpaceAccessorProvider),
            canRemoveFromSharedCollection = shareEnabled,
            collectionLimit = collectionLimit
        )

        if (items.isNotEmpty()) {
            _uiState.emit(ViewState.List(viewData))
        } else {
            _uiState.emit(ViewState.Empty(viewData))
        }
    }

    private fun SummaryObject.toSummaryForUi(): SummaryForUi? {
        return when (this) {
            is SummaryObject.Authentifiant -> {
                SummaryForUi(
                    id = id,
                    type = syncObjectType.xmlObjectName,
                    thumbnail = urlDomain?.toUrlDomainOrNull(),
                    firstLine = titleForListNormalized ?: "",
                    secondLine = loginForUi ?: "",
                    sharingPermission = if (allowAllRoles) sharingPermission else null,
                    spaceData = spaceData(teamSpaceAccessorProvider)
                )
            }

            else -> null
        }
    }

    fun deleteClicked() {
        viewModelScope.launch {
            _uiState.emit(ViewState.DeletePrompt(_uiState.value.viewData))
        }
    }

    fun confirmDeleteClicked() {
        viewModelScope.launch {
            val collection = collectionDataQuery.queryById(navArgs.collectionId)
            if (collection != null) {
                val collectionToDelete = collection.copy(syncState = SyncState.DELETED)
                dataSaver.save(collectionToDelete)
                vaultActivityLogger.sendCollectionDeletedActivityLog(
                    collection = collectionToDelete.toSummary()
                )
            }
        }
    }

    fun dismissDeleteClicked() {
        viewModelScope.launch {
            _uiState.emit(ViewState.List(_uiState.value.viewData))
        }
    }

    fun removeItem(itemId: String, shared: Boolean) {
        viewModelScope.launch {
            if (shared) {
                val collection =
                    sharingDataProvider.getCollections(itemId, needsAdminRights = false)
                        .firstOrNull { it.uuid == navArgs.collectionId } ?: return@launch
                collectionSharingItemDataProvider.removeItemFromSharedCollections(
                    itemId,
                    listOf(collection)
                )
                return@launch
            }
            val collection = collectionDataQuery.queryById(navArgs.collectionId)
            if (collection != null) {
                val updatedCollection = collection.copySyncObject {
                    vaultItems = vaultItems?.filterNot { it.id == itemId }
                }.copyWithAttrs {
                    syncState = SyncState.MODIFIED
                }
                dataSaver.save(updatedCollection)

                val removedItem = credentialDataQuery.queryFirst { specificUid(itemId) }
                if (removedItem != null) {
                    vaultActivityLogger.sendRemoveItemFromCollectionActivityLog(
                        collection = updatedCollection.toSummary(),
                        item = removedItem
                    )
                }
            }
        }
    }
}