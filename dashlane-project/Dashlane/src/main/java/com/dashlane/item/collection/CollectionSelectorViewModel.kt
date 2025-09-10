package com.dashlane.item.collection

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.item.v3.data.toCollectionData
import com.dashlane.item.v3.repositories.CollectionsRepository
import com.dashlane.mvvm.MutableViewStateFlow
import com.dashlane.mvvm.ViewStateFlow
import com.dashlane.sharingpolicy.SharingPolicyDataProvider
import com.dashlane.storage.userdata.accessor.GenericDataQuery
import com.dashlane.storage.userdata.accessor.VaultDataQuery
import com.dashlane.storage.userdata.accessor.filter.GenericFilter
import com.dashlane.storage.userdata.accessor.filter.VaultFilter
import com.dashlane.utils.coroutines.inject.qualifiers.IoCoroutineDispatcher
import com.dashlane.vault.model.COLLECTION_NAME_MAX_LENGTH
import com.dashlane.vault.model.toSanitizedCollectionName
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CollectionSelectorViewModel @Inject constructor(
    private val collectionSelectorDataProvider: CollectionSelectorDataProvider,
    private val collectionsRepository: CollectionsRepository,
    private val vaultDataQuery: VaultDataQuery,
    private val sharingPolicyDataProvider: SharingPolicyDataProvider,
    private val genericDataQuery: GenericDataQuery,
    @IoCoroutineDispatcher
    private val ioDispatcher: CoroutineDispatcher,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val _uiState = MutableViewStateFlow<CollectionSelectorState.View, CollectionSelectorState.SideEffect>(
        CollectionSelectorState.View(itemId = CollectionSelectorFragmentArgs.fromSavedStateHandle(savedStateHandle).itemId),
    )
    val uiState: ViewStateFlow<CollectionSelectorState.View, CollectionSelectorState.SideEffect> = _uiState

    fun onViewStarted() {
        viewModelScope.launch {
            updateSuggestedCollections(uiState.viewState.value.userPrompt)
        }
    }

    fun updateUserPrompt(prompt: String) {
        val userPrompt = prompt.take(COLLECTION_NAME_MAX_LENGTH)
        _uiState.update {
            it.copy(
                userPrompt = userPrompt,
            )
        }
        viewModelScope.launch {
            updateSuggestedCollections(prompt)
        }
    }

    @VisibleForTesting
    fun showDialogConfirmAddToSharedCollection(collection: CollectionSelectorState.Collection) {
        _uiState.update {
            val summaryObject = genericDataQuery.queryFirst(GenericFilter(it.itemId)) ?: return@update it
            val isLimited = !sharingPolicyDataProvider.canShareItem(summaryObject = summaryObject)
            it.copy(
                confirmAddToSharedCollectionDialogData = CollectionSelectorState.ConfirmAddToSharedCollectionDialogData(
                    collection = collection,
                    isLimited = isLimited,
                ),
            )
        }
    }

    fun onClickCollection(collection: CollectionSelectorState.Collection) {
        if (collection.shared && !collection.selected) {
            showDialogConfirmAddToSharedCollection(collection)
            return
        }
        _uiState.update {
            toggleCollection(it, collection)
        }
    }

    fun confirmAddToSharedCollection(collection: CollectionSelectorState.Collection) {
        _uiState.update {
            toggleCollection(it, collection).copy(confirmAddToSharedCollectionDialogData = null)
        }
    }

    fun cancelAddToSharedCollection() {
        _uiState.update { it.copy(confirmAddToSharedCollectionDialogData = null) }
    }

    @VisibleForTesting
    fun toggleCollection(
        currentState: CollectionSelectorState.View,
        collection: CollectionSelectorState.Collection,
    ): CollectionSelectorState.View {
        val updatedCollection = collection.copy(selected = !collection.selected, forCreation = false)
        currentState.collections ?: return currentState
        return currentState.copy(
            collections = (currentState.collections - collection + updatedCollection).sort(),
            collectionsFilteredForDisplay = (currentState.collectionsFilteredForDisplay - collection + updatedCollection).sort(),
        )
    }

    private suspend fun updateSuggestedCollections(prompt: String) {
        _uiState.update { state ->
            val collections =
                uiState.viewState.value.collections ?: collectionSelectorDataProvider.fetchExistingCollections(
                    state.itemId,
                ).sort()
            val sanitizedPrompt = prompt.toSanitizedCollectionName()
            val promptMatchingCollections = (
                collections +
                    CollectionSelectorState.Collection(
                        id = null,
                        name = sanitizedPrompt,
                        shared = false,
                        selected = false,
                        initiallySelected = false,
                        forCreation = true,
                    ).takeIf { canCreate(collections, sanitizedPrompt) }
            )
                .filterNotNull()
                .filterForPrompt(sanitizedPrompt)
                .sort()
            state.copy(
                collections = collections,
                collectionsFilteredForDisplay = promptMatchingCollections.toList(),
            )
        }
    }

    private fun List<CollectionSelectorState.Collection>.filterForPrompt(prompt: String) =
        filter { collection ->
            collection.name.contains(prompt, ignoreCase = true)
        }

    private fun canCreate(collections: List<CollectionSelectorState.Collection>, userPrompt: String) =
        userPrompt.isNotEmpty() && collections.filterNot { it.shared }.none { it.name == userPrompt }

    private fun List<CollectionSelectorState.Collection>.sort() = sortedWith(
        compareBy<CollectionSelectorState.Collection> {
            it.name
        }.thenBy {
            it.shared
        },
    )

    fun save() {
        viewModelScope.launch(ioDispatcher) {
            val vaultItem = vaultDataQuery.query(
                VaultFilter(
                    uid = _uiState.viewState.value.itemId,
                ),
            )
            if (vaultItem == null) {
                
                return@launch
            }

            val allCollections = _uiState.viewState.value.collections
            if (allCollections != null) {
                collectionsRepository.saveCollections(
                    vaultItem,
                    allCollections.filter { it.selected }.map { it.toCollectionData() },
                )
            }
            _uiState.send(CollectionSelectorState.SideEffect.Close(hasSaved = true))
        }
    }

    fun onBackPressed() {
        if (hasPendingChanges()) {
            _uiState.update {
                it.copy(
                    pendingChangeDialogData = CollectionSelectorState.PendingChangeDialogData,
                )
            }
        } else {
            close()
        }
    }

    @VisibleForTesting
    fun hasPendingChanges(): Boolean = _uiState.viewState.value.collections?.any {
        it.selected != it.initiallySelected
    } ?: false

    fun cancelPendingChangeDialog() {
        _uiState.update {
            it.copy(
                pendingChangeDialogData = null,
            )
        }
    }

    fun discardPendingChangeDialog() {
        close()
    }

    private fun close() {
        viewModelScope.launch {
            _uiState.send(CollectionSelectorState.SideEffect.Close(hasSaved = false))
        }
    }
}