package com.dashlane.item.collection

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.item.collection.CollectionSelectorViewModel.UiState.ShowConfirmDialog
import com.dashlane.item.subview.ItemCollectionListSubView.Collection
import com.dashlane.storage.userdata.accessor.CollectionDataQuery
import com.dashlane.storage.userdata.accessor.filter.CollectionFilter
import com.dashlane.teamspaces.manager.TeamSpaceAccessorProvider
import com.dashlane.teamspaces.model.TeamSpace
import com.dashlane.ui.screens.fragments.userdata.sharing.center.SharingDataProvider
import com.dashlane.userfeatures.FeatureFlip
import com.dashlane.userfeatures.UserFeaturesChecker
import com.dashlane.vault.model.COLLECTION_NAME_MAX_LENGTH
import com.dashlane.vault.model.toSanitizedCollectionName
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CollectionSelectorViewModel @Inject constructor(
    private val collectionDataQuery: CollectionDataQuery,
    private val teamSpaceAccessorProvider: TeamSpaceAccessorProvider,
    private val sharingDataProvider: SharingDataProvider,
    private val userFeaturesChecker: UserFeaturesChecker,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _suggestedCollections = MutableStateFlow(listOf<Collection>())
    val collections: StateFlow<List<Collection>> = _suggestedCollections.asStateFlow()
    private val _uiState = MutableStateFlow<UiState>(UiState.Nothing)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    val canCreate: Boolean
        get() = userPrompt.isNotEmpty() &&
            getAllCollectionsInItemSpace().none { it.name == userPrompt } &&
            navArgs.temporaryPrivateCollectionsName.none { it == userPrompt }

    var userPrompt by mutableStateOf("")
        private set

    private val navArgs = CollectionSelectorActivityArgs.fromSavedStateHandle(savedStateHandle)

    init {
        viewModelScope.launch {
            updateSuggestedCollections(userPrompt)
        }
    }

    fun updateUserPrompt(prompt: String) {
        userPrompt = prompt.take(COLLECTION_NAME_MAX_LENGTH)
        viewModelScope.launch {
            updateSuggestedCollections(prompt)
        }
    }

    fun confirmAddToSharedCollection(collection: Collection) {
        _uiState.tryEmit(ShowConfirmDialog(collection))
    }

    fun cancelAddToSharedCollection() {
        _uiState.tryEmit(UiState.Nothing)
    }

    private fun getAllCollectionsInItemSpace() = collectionDataQuery.queryAll(
        CollectionFilter().apply {
            specificSpace(
                teamSpaceAccessorProvider.get()?.get(navArgs.spaceId) ?: TeamSpace.Personal
            )
        }
    )

    private suspend fun updateSuggestedCollections(prompt: String) {
        val privateCollections = getAllCollectionsInItemSpace()
            .mapNotNull {
                val name = it.name ?: return@mapNotNull null
                Collection(it.id, name, false)
            }
        val sharedCollections =
            if (userFeaturesChecker.has(FeatureFlip.SHARING_COLLECTION)) {
                if (navArgs.spaceId == TeamSpace.Personal.teamId) {
                    emptyList()
                } else {
                    
                    
                    val showAll = userFeaturesChecker.has(FeatureFlip.SHARING_COLLECTION_ROLES)
                    sharingDataProvider.getAcceptedCollections(needsAdminRights = !showAll).map {
                        Collection(it.uuid, it.name, true)
                    }
                }
            } else {
                emptyList()
            }
        val collections = (privateCollections + sharedCollections)
            .filterForPrompt(prompt.toSanitizedCollectionName())
            .filterNotLinkedToItem()
            .sortedBy { it.name }
        _suggestedCollections.update { collections.toList() }
    }

    private fun List<Collection>.filterForPrompt(prompt: String) =
        filter { collection ->
            collection.name.contains(prompt, ignoreCase = true)
        }

    private fun List<Collection>.filterNotLinkedToItem() =
        filterNot { collection ->
            (!collection.shared && navArgs.temporaryPrivateCollectionsName.contains(collection.name)) ||
                (collection.shared && navArgs.temporarySharedCollectionsId.contains(collection.id))
        }

    sealed class UiState {
        data object Nothing : UiState()
        data class ShowConfirmDialog(val collection: Collection) : UiState()
    }
}