package com.dashlane.collections.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.collections.businessSpaceData
import com.dashlane.collections.sharing.item.CollectionSharingItemDataProvider
import com.dashlane.collections.spaceData
import com.dashlane.securefile.extensions.hasAttachments
import com.dashlane.storage.userdata.accessor.CollectionDataQuery
import com.dashlane.storage.userdata.accessor.CredentialDataQuery
import com.dashlane.storage.userdata.accessor.DataSaver
import com.dashlane.storage.userdata.accessor.queryFirst
import com.dashlane.teamspaces.manager.TeamSpaceAccessorProvider
import com.dashlane.ui.screens.fragments.userdata.sharing.center.SharingDataProvider
import com.dashlane.utils.coroutines.inject.qualifiers.DefaultCoroutineDispatcher
import com.dashlane.vault.VaultActivityLogger
import com.dashlane.vault.model.SyncState
import com.dashlane.vault.model.copySyncObject
import com.dashlane.vault.model.loginForUi
import com.dashlane.vault.model.titleForListNormalized
import com.dashlane.vault.model.urlForUI
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.summary.toSummary
import com.dashlane.xml.domain.SyncObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
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
    private val collectionLimiter: CollectionLimiter,
    private val vaultActivityLogger: VaultActivityLogger
) : ViewModel() {
    
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
                sections = emptyList(),
                spaceData = null,
                editAllowed = false,
                deleteAllowed = false,
                hasItemWithAttachment = false
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
        viewModelScope.launch {
            sharingDataProvider.updatedItemFlow.collect {
                refreshCollectionDetails()
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
            if (sharedCollection) {
                refreshSharedCollection()
            } else {
                refreshPrivateCollection()
            }
        }
    }

    private suspend fun refreshSharedCollection() {
        val collections =
            sharingDataProvider.getAcceptedCollections(needsAdminRights = false).filter {
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
            sections = items.toItemSections(),
            spaceData = businessSpaceData(teamSpaceAccessorProvider),
            collectionLimit = collectionLimit,
            editAllowed = sharingDataProvider.isAdmin(collection),
            deleteAllowed = sharingDataProvider.isAdmin(collection),
            hasItemWithAttachment = items.any { it.hasAttachments() }
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
            sections = items.toItemSections(),
            spaceData = collection.syncObject.spaceData(teamSpaceAccessorProvider),
            collectionLimit = collectionLimit,
            editAllowed = true,
            deleteAllowed = true,
            hasItemWithAttachment = items.any { it.hasAttachments() }
        )

        if (items.isNotEmpty()) {
            _uiState.emit(ViewState.List(viewData))
        } else {
            _uiState.emit(ViewState.Empty(viewData))
        }
    }

    private fun List<SummaryObject>.toItemSections() =
        groupBy { it::class.java }.mapNotNull { entry ->
            val sectionType = when (entry.key) {
                SummaryObject.Authentifiant::class.java -> SectionType.SECTION_LOGIN
                SummaryObject.SecureNote::class.java -> SectionType.SECTION_SECURE_NOTE
                else -> null
            }
            sectionType?.let { type ->
                ItemSection(
                    sectionType = type,
                    items = entry.value.mapNotNull { it.toSummaryForUi() }
                )
            }
        }.sortedBy {
            it.sectionType.index
        }

    private fun SummaryObject.toSummaryForUi(): SummaryForUi? {
        return when (this) {
            is SummaryObject.Authentifiant -> {
                SummaryForUi(
                    id = id,
                    type = syncObjectType.xmlObjectName,
                    thumbnail = ThumbnailData.UrlThumbnail(url = urlForUI()),
                    firstLine = titleForListNormalized ?: "",
                    secondLine = ContentLine2.Text(loginForUi ?: ""),
                    sharingPermission = sharingPermission,
                    spaceData = spaceData(teamSpaceAccessorProvider)
                )
            }
            is SummaryObject.SecureNote -> {
                SummaryForUi(
                    id = id,
                    type = syncObjectType.xmlObjectName,
                    thumbnail = ThumbnailData.SecureNoteThumbnail(
                        secureNoteType = type ?: SyncObject.SecureNoteType.NO_TYPE
                    ),
                    firstLine = title ?: "",
                    secondLine = if (secured == true) {
                        ContentLine2.SecureNoteSecured
                    } else {
                        ContentLine2.Text(content?.lineSequence()?.firstOrNull()?.take(n = 80) ?: "")
                    },
                    sharingPermission = sharingPermission,
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

    fun confirmDeleteClicked(shared: Boolean) {
        viewModelScope.launch {
            if (shared) {
                deleteSharedCollection()
                return@launch
            }
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

    private suspend fun deleteSharedCollection() {
        val collections =
            sharingDataProvider.getAcceptedCollections(needsAdminRights = false).filter {
                it.uuid == navArgs.collectionId
            }
        if (collections.isEmpty()) {
            _uiState.emit(ViewState.Deleted(_uiState.value.viewData))
            return
        }
        val collection = collections.first()

        if (!sharingDataProvider.isDeleteAllowed(collection)) {
            _uiState.emit(ViewState.RevokeAccessPrompt(_uiState.value.viewData))
            return
        }

        runCatching {
            sharingDataProvider.deleteCollection(collection, true)
        }.onSuccess {
            _uiState.update { ViewState.Deleted(it.viewData) }
        }.onFailure {
            _uiState.update {
                ViewState.SharedCollectionDeleteError(viewData = it.viewData)
            }
        }
    }

    fun dismissDialogClicked() {
        viewModelScope.launch {
            _uiState.emit(ViewState.List(_uiState.value.viewData))
        }
    }

    fun removeItem(itemId: String, shared: Boolean) {
        viewModelScope.launch {
            if (shared) {
                val collection =
                    sharingDataProvider.getCollections(itemId)
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

    fun attemptSharingWithAttachment() {
        _uiState.update {
            ViewState.SharingWithAttachmentError(it.viewData)
        }
    }
}