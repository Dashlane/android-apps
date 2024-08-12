package com.dashlane.collections.edit

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.core.sharing.SharingItemUpdater
import com.dashlane.core.sharing.SharingItemUpdaterRequest
import com.dashlane.network.tools.authorization
import com.dashlane.server.api.endpoints.sharinguserdevice.RenameCollectionService
import com.dashlane.server.api.pattern.UuidFormat
import com.dashlane.session.SessionManager
import com.dashlane.storage.userdata.accessor.CollectionDataQuery
import com.dashlane.storage.userdata.accessor.DataSaver
import com.dashlane.storage.userdata.accessor.filter.collectionFilter
import com.dashlane.teamspaces.manager.TeamSpaceAccessorProvider
import com.dashlane.teamspaces.model.TeamSpace
import com.dashlane.ui.screens.fragments.userdata.sharing.center.SharingDataProvider
import com.dashlane.ui.screens.fragments.userdata.sharing.center.SharingDataUpdateProvider
import com.dashlane.utils.coroutines.inject.qualifiers.DefaultCoroutineDispatcher
import com.dashlane.vault.VaultActivityLogger
import com.dashlane.vault.model.COLLECTION_NAME_MAX_LENGTH
import com.dashlane.vault.model.CommonDataIdentifierAttrsImpl
import com.dashlane.vault.model.SyncState
import com.dashlane.vault.model.copySyncObject
import com.dashlane.vault.model.createCollection
import com.dashlane.vault.model.toSanitizedCollectionName
import com.dashlane.vault.summary.toSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CollectionsEditViewModel @Inject constructor(
    private val teamSpaceAccessorProvider: TeamSpaceAccessorProvider,
    private val collectionDataQuery: CollectionDataQuery,
    private val dataSaver: DataSaver,
    private val vaultActivityLogger: VaultActivityLogger,
    @DefaultCoroutineDispatcher private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val sharingDataProvider: SharingDataProvider,
    private val sharingDataUpdateProvider: SharingDataUpdateProvider,
    private val sharingItemUpdater: SharingItemUpdater,
    private val renameCollectionService: RenameCollectionService,
    private val sessionManager: SessionManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val navArgs = CollectionEditFragmentArgs.fromSavedStateHandle(savedStateHandle)
    private val editMode = navArgs.collectionId != null

    private val _uiState = MutableStateFlow<ViewState>(
        ViewState.Loading(
            ViewData(
                collectionName = TextFieldValue(text = ""),
                editMode = editMode,
                space = null,
                availableSpaces = null
            )
        )
    )
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch(defaultDispatcher) {
            loadExistingCollectionData()
            refreshAvailableSpaces()
        }
    }

    private suspend fun loadExistingCollectionData() {
        val collectionId = navArgs.collectionId ?: return

        val sharedCollection = navArgs.sharedCollection
        if (sharedCollection) {
            refreshSharedCollection(collectionId)
        } else {
            refreshPrivateCollection(collectionId)
        }
    }

    private fun refreshPrivateCollection(collectionId: String) {
        val collection = collectionDataQuery.queryById(collectionId)
        val spaceId = collection?.syncObject?.spaceId
        val space = if (spaceId != null) {
            teamSpaceAccessorProvider.get()?.let { teamSpaceAccessor ->
                if (teamSpaceAccessor.canChangeTeamspace) {
                    teamSpaceAccessor.currentBusinessTeam.takeIf { it?.teamId == spaceId }
                } else {
                    null
                }
            }
        } else {
            null
        }

        _uiState.update { viewState ->
            val collectionName = collection?.syncObject?.name ?: viewState.viewData.collectionName.text
            ViewState.Loading(
                viewState.viewData.copy(
                    collectionName = TextFieldValue(
                        text = collectionName,
                        selection = TextRange(collectionName.length)
                    ),
                    space = space
                )
            )
        }
    }

    private suspend fun refreshSharedCollection(collectionId: String) {
        val collection =
            sharingDataProvider.getAcceptedCollections(needsAdminRights = true).firstOrNull { it.uuid == collectionId }

        _uiState.update { viewState ->
            val collectionName = collection?.name ?: viewState.viewData.collectionName.text
            ViewState.Loading(
                viewState.viewData.copy(
                    collectionName = TextFieldValue(
                        text = collectionName,
                        selection = TextRange(collectionName.length)
                    ),
                    
                    space = teamSpaceAccessorProvider.get()?.currentBusinessTeam
                )
            )
        }
    }

    private fun refreshAvailableSpaces() {
        val spaces = teamSpaceAccessorProvider.get()?.let { teamSpaceAccessor ->
            if (teamSpaceAccessor.canChangeTeamspace) {
                teamSpaceAccessor.availableSpaces.minus(TeamSpace.Combined)
            } else {
                null
            }
        }
        _uiState.update { viewState ->
            ViewState.Form(
                viewState.viewData.copy(
                    availableSpaces = spaces,
                    space = viewState.viewData.space.takeIf { it in (spaces ?: emptyList()) }
                        ?: TeamSpace.Personal
                )
            )
        }
    }

    fun onNameChanged(name: TextFieldValue) {
        val constrainedName = name.copy(text = name.text.take(COLLECTION_NAME_MAX_LENGTH))
        _uiState.update {
            if (it is ViewState.Form || it is ViewState.Error) {
                ViewState.Form(it.viewData.copy(collectionName = constrainedName))
            } else {
                it
            }
        }
    }

    fun onSpaceSelected(teamspace: TeamSpace) {
        _uiState.update {
            if (it is ViewState.Form || it is ViewState.Error) {
                ViewState.Form(it.viewData.copy(space = teamspace))
            } else {
                it
            }
        }
    }

    fun saveClicked() {
        val name = _uiState.value.viewData.collectionName.text.toSanitizedCollectionName()
        val space = _uiState.value.viewData.space

        
        if (name.isEmpty()) {
            _uiState.update {
                ViewState.Error(viewData = it.viewData, fieldError = FieldError.EMPTY_NAME)
            }
            return
        }

        _uiState.update {
            ViewState.Saving(it.viewData)
        }

        viewModelScope.launch(defaultDispatcher) {
            val collectionId = navArgs.collectionId
            if (navArgs.sharedCollection) {
                if (collectionId != null) {
                    saveSharedCollection(name, collectionId)
                } else {
                    
                }
            } else {
                savePrivateCollection(name, space, collectionId)
            }
        }
    }

    private suspend fun saveSharedCollection(name: String, collectionId: String) {
        val oldCollection =
            sharingDataProvider.getAcceptedCollections(needsAdminRights = true).firstOrNull {
                it.uuid == collectionId
            } ?: return
        val collection = sharingDataUpdateProvider.getUpdatedCollection(collection = oldCollection) ?: oldCollection

        runCatching {
            renameCollectionService.execute(
                sessionManager.session!!.authorization,
                RenameCollectionService.Request(
                    UuidFormat(collectionId),
                    name,
                    collection.revision
                )
            )
        }.onSuccess { response ->
            response.data.collections?.let { collections ->
                sharingItemUpdater.update(SharingItemUpdaterRequest(collectionUpdates = collections))
            }
            _uiState.update {
                ViewState.Saved(it.viewData)
            }
        }.onFailure {
            _uiState.update {
                ViewState.Error(
                    viewData = it.viewData,
                    fieldError = null,
                    snackError = SnackError.SHARED_COLLECTION_RENAME_GENERIC_ERROR
                )
            }
        }
    }

    private suspend fun savePrivateCollection(
        name: String,
        space: TeamSpace?,
        collectionId: String?
    ) {
        val existingCollection =
            collectionDataQuery.queryByName(name, collectionFilter { space?.let { specificSpace(space) } })
        if (existingCollection != null && existingCollection.uid != collectionId) {
            _uiState.update {
                ViewState.Error(viewData = it.viewData, fieldError = FieldError.COLLECTION_ALREADY_EXISTS)
            }
        } else {
            
            val collection = if (collectionId == null) {
                createCollection(
                    dataIdentifier = CommonDataIdentifierAttrsImpl(
                        teamSpaceId = space?.teamId ?: TeamSpace.Personal.teamId
                    ),
                    name = name,
                    vaultItems = emptyList()
                ).also { collection ->
                    vaultActivityLogger.sendCollectionCreatedActivityLog(collection = collection.toSummary())
                }
            } else {
                
                val collection = collectionDataQuery.queryById(collectionId)
                collection?.let {
                    it.copySyncObject {
                        this.name = name
                    }.copyWithAttrs {
                        syncState = SyncState.MODIFIED
                    }
                }?.also { editedCollection ->
                    vaultActivityLogger.sendCollectionRenamedActivityLog(
                        collection = editedCollection.toSummary(),
                        previousName = collection.syncObject.name
                    )
                }
            }

            if (collection != null) {
                dataSaver.save(collection)
            }

            _uiState.update {
                ViewState.Saved(it.viewData)
            }
        }
    }
}