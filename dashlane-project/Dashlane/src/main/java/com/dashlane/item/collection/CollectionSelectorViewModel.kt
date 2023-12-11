package com.dashlane.item.collection

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.dashlane.storage.userdata.accessor.MainDataAccessor
import com.dashlane.storage.userdata.accessor.filter.CollectionFilter
import com.dashlane.teamspaces.PersonalTeamspace
import com.dashlane.teamspaces.manager.TeamspaceAccessor
import com.dashlane.util.inject.OptionalProvider
import com.dashlane.vault.model.COLLECTION_NAME_MAX_LENGTH
import com.dashlane.vault.model.toSanitizedCollectionName
import com.dashlane.vault.summary.SummaryObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.text.Collator
import javax.inject.Inject

@HiltViewModel
class CollectionSelectorViewModel @Inject constructor(
    private val mainDataAccessor: MainDataAccessor,
    private val teamspaceAccessorProvider: OptionalProvider<TeamspaceAccessor>,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _suggestedCollections = MutableStateFlow(listOf<SummaryObject.Collection>())
    val collections: StateFlow<List<SummaryObject.Collection>> = _suggestedCollections.asStateFlow()

    val canCreate: Boolean
        get() = userPrompt.isNotEmpty() &&
                getAllCollections().none { it.name == userPrompt } &&
                navArgs.temporaryCollections.none { it == userPrompt }

    var userPrompt by mutableStateOf("")
        private set

    private val navArgs = CollectionSelectorActivityArgs.fromSavedStateHandle(savedStateHandle)

    init {
        updateSuggestedCollections(userPrompt)
    }

    fun updateUserPrompt(prompt: String) {
        userPrompt = prompt.take(COLLECTION_NAME_MAX_LENGTH)
        updateSuggestedCollections(prompt)
    }

    private fun getAllCollections() = mainDataAccessor.getCollectionDataQuery().queryAll(
        CollectionFilter().apply {
        specificSpace(teamspaceAccessorProvider.get()?.get(navArgs.spaceId) ?: PersonalTeamspace)
    }
    )

    private fun updateSuggestedCollections(prompt: String) {
        val collections = getAllCollections()
            .filterForPrompt(prompt.toSanitizedCollectionName())
            .filterNotLinkedToItem()
            .sortedWith(
                compareBy(Collator.getInstance()) {
                    it.name
                }
            )
        _suggestedCollections.update {
            collections.toList()
        }
    }

    private fun List<SummaryObject.Collection>.filterForPrompt(prompt: String) =
        filter { collection ->
            collection.name?.contains(prompt, ignoreCase = true) ?: false
        }

    private fun List<SummaryObject.Collection>.filterNotLinkedToItem() =
        filterNot { collection ->
            navArgs.temporaryCollections.contains(collection.name)
        }
}