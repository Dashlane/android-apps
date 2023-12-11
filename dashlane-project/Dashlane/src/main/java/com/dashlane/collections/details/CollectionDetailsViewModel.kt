package com.dashlane.collections.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.collections.businessSpaceData
import com.dashlane.collections.spaceData
import com.dashlane.storage.userdata.accessor.MainDataAccessor
import com.dashlane.teamspaces.manager.TeamspaceAccessorProvider
import com.dashlane.ui.screens.fragments.userdata.sharing.center.SharingDataProvider
import com.dashlane.url.toUrlDomainOrNull
import com.dashlane.util.inject.qualifiers.DefaultCoroutineDispatcher
import com.dashlane.util.userfeatures.UserFeaturesChecker
import com.dashlane.util.userfeatures.UserFeaturesChecker.FeatureFlip
import com.dashlane.vault.model.SyncState
import com.dashlane.vault.model.loginForUi
import com.dashlane.vault.model.titleForListNormalized
import com.dashlane.vault.model.urlDomain
import com.dashlane.vault.summary.SummaryObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CollectionDetailsViewModel @Inject constructor(
    private val teamspaceAccessorProvider: TeamspaceAccessorProvider,
    @DefaultCoroutineDispatcher private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default,
    mainDataAccessor: MainDataAccessor,
    savedStateHandle: SavedStateHandle,
    private val sharingDataProvider: SharingDataProvider,
    private val userFeaturesChecker: UserFeaturesChecker
) : ViewModel() {
    private val collectionDataQuery = mainDataAccessor.getCollectionDataQuery()
    private val dataSaver = mainDataAccessor.getDataSaver()
    private val shareEnabled: Boolean
        get() = userFeaturesChecker.has(FeatureFlip.SHARING_COLLECTION_MILESTONE_1)
    val navArgs = CollectionDetailsFragmentArgs.fromSavedStateHandle(savedStateHandle)
    var listeningChanges = true

    private val _uiState = MutableStateFlow<ViewState>(
        ViewState.Loading(
            ViewData(
                collectionName = null,
                shared = false,
                items = emptyList(),
                spaceData = null
            )
        )
    )
    val uiState = _uiState.asStateFlow()

    init {
        refreshCollectionDetails()
        viewModelScope.launch {
            mainDataAccessor.getDataSaver().savedItemFlow.collect {
                
                
                if (listeningChanges) refreshCollectionDetails()
            }
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
            sharingDataProvider.getAcceptedCollections().filter { it.uuid == navArgs.collectionId }
        if (collections.isEmpty()) {
            _uiState.emit(ViewState.Deleted(_uiState.value.viewData))
            return
        }
        val collection = collections.first()
        val title = collection.name
        val items = sharingDataProvider.getAcceptedCollectionsItems(navArgs.collectionId)
        val viewData = ViewData(
            collectionName = title,
            shared = true,
            items = items.mapNotNull { it.toSummaryForUi() },
            spaceData = businessSpaceData(teamspaceAccessorProvider)
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
        val viewData = ViewData(
            collectionName = title,
            shared = false,
            items = items.mapNotNull { it.toSummaryForUi() },
            spaceData = collection.syncObject.spaceData(teamspaceAccessorProvider)
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
                    spaceData = spaceData(teamspaceAccessorProvider)
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
            }
        }
    }

    fun dismissDeleteClicked() {
        viewModelScope.launch {
            _uiState.emit(ViewState.List(_uiState.value.viewData))
        }
    }

    fun removeItem(itemId: String) {
        viewModelScope.launch {
            val collection = collectionDataQuery.queryById(navArgs.collectionId)
            if (collection != null) {
                val updatedCollection = collection.copy(
                    syncObject = collection.syncObject.copy {
                        vaultItems = vaultItems?.filterNot { it.id == itemId }
                    }
                )
                dataSaver.save(updatedCollection)
            }
        }
    }
}