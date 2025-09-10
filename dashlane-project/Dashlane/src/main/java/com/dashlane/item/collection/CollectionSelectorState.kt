package com.dashlane.item.collection

import com.dashlane.mvvm.State

sealed class CollectionSelectorState : State {
    data class View(
        val itemId: String,
        val userPrompt: String = "",
        val collections: List<Collection>? = null,
        val collectionsFilteredForDisplay: List<Collection> = emptyList(),
        val confirmAddToSharedCollectionDialogData: ConfirmAddToSharedCollectionDialogData? = null,
        val pendingChangeDialogData: PendingChangeDialogData? = null,
    ) : CollectionSelectorState(), State.View

    sealed class SideEffect : CollectionSelectorState(), State.SideEffect {
        data class Close(val hasSaved: Boolean) : SideEffect()
    }

    data class ConfirmAddToSharedCollectionDialogData(val collection: Collection, val isLimited: Boolean)

    data object PendingChangeDialogData

    data class Collection(
        val id: String? = null,
        val name: String,
        val shared: Boolean,
        val selected: Boolean = false,
        val initiallySelected: Boolean = false,
        val forCreation: Boolean = false,
    )
}