package com.dashlane.collections.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.collections.businessSpaceData
import com.dashlane.collections.spaceData
import com.dashlane.server.api.endpoints.sharinguserdevice.Collection
import com.dashlane.storage.userdata.accessor.MainDataAccessor
import com.dashlane.storage.userdata.accessor.filter.CollectionFilter
import com.dashlane.teamspaces.PersonalTeamspace
import com.dashlane.teamspaces.manager.TeamspaceAccessorProvider
import com.dashlane.teamspaces.manager.TeamspaceManager
import com.dashlane.teamspaces.manager.TeamspaceManagerWeakListener
import com.dashlane.teamspaces.model.Teamspace
import com.dashlane.ui.screens.fragments.userdata.sharing.center.SharingDataProvider
import com.dashlane.util.userfeatures.UserFeaturesChecker
import com.dashlane.util.userfeatures.UserFeaturesChecker.FeatureFlip
import com.dashlane.vault.model.SyncState
import com.dashlane.vault.summary.SummaryObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CollectionsListViewModel @Inject constructor(
    private val teamspaceAccessorProvider: TeamspaceAccessorProvider,
    private val sharingDataProvider: SharingDataProvider,
    private val userFeaturesChecker: UserFeaturesChecker,
    mainDataAccessor: MainDataAccessor
) : ViewModel() {
    private val _uiState = MutableStateFlow<ViewState>(ViewState.Loading(ViewData(emptyList())))
    val uiState = _uiState.asStateFlow()

    private val collectionDataQuery = mainDataAccessor.getCollectionDataQuery()

    private val dataSaver = mainDataAccessor.getDataSaver()

    private val teamspaceChangeListener = object : TeamspaceManager.Listener {
        override fun onStatusChanged(
            teamspace: Teamspace?,
            previousStatus: String?,
            newStatus: String?
        ) {
            
        }

        override fun onChange(teamspace: Teamspace?) {
            refreshCollections()
        }

        override fun onTeamspacesUpdate() {
            
        }
    }

    private val teamspaceManagerListener = TeamspaceManagerWeakListener(teamspaceChangeListener)

    private val shareEnabled: Boolean
        get() = userFeaturesChecker.has(FeatureFlip.SHARING_COLLECTION_MILESTONE_1)

    init {
        refreshCollections()
        teamspaceManagerListener.listen(teamspaceAccessorProvider.get())
        viewModelScope.launch {
            mainDataAccessor.getDataSaver().savedItemFlow.collect {
                refreshCollections()
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
            val sharedCollections = if (shareEnabled) {
                sharingDataProvider.getAcceptedCollections()
            } else {
                emptyList()
            }
            val allCollections =
                collections.mapNotNull { it.toCollectionViewData() } + sharedCollections.mapNotNull {
                    val size = sharingDataProvider.getAcceptedCollectionsItems(it.uuid).size
                    it.toCollectionViewData(size)
                }
            val viewData = ViewData(allCollections.sortedBy { it.name })
            if (viewData.collections.isNotEmpty()) {
                _uiState.emit(ViewState.List(viewData))
            } else {
                _uiState.emit(ViewState.Empty(viewData))
            }
        }
    }

    private fun SummaryObject.Collection.toCollectionViewData() =
        name?.let { name ->
            val spaceData = spaceData(teamspaceAccessorProvider)
            CollectionViewData(
                id = id,
                name = name,
                itemCount = vaultItems?.size ?: 0,
                spaceData = spaceData,
                shared = false,
                shareAllowed = shareEnabled && spaceData?.businessSpace == true
            )
        }

    private suspend fun Collection.toCollectionViewData(size: Int): CollectionViewData? {
        if (teamspaceAccessorProvider.get()?.isCurrent(PersonalTeamspace.teamId!!) == true) {
            
            return null
        }
        return CollectionViewData(
            id = uuid,
            name = name,
            itemCount = size,
            spaceData = businessSpaceData(teamspaceAccessorProvider),
            shared = true,
            shareAllowed = shareEnabled && sharingDataProvider.isCollectionShareAllowed(this)
        )
    }

    fun deleteClicked(id: String) {
        viewModelScope.launch {
            val collection = collectionDataQuery.queryById(id)
            if (collection != null) {
                val collectionToDelete = collection.copy(syncState = SyncState.DELETED)
                dataSaver.save(collectionToDelete)
            }
        }
    }
}