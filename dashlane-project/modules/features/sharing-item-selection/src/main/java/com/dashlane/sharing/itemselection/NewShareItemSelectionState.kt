package com.dashlane.sharing.itemselection

import com.dashlane.mvvm.State
import com.dashlane.udf.Event
import com.dashlane.vault.item.VaultListItemState

sealed class ListItemState {
    abstract val key: String

    data class HeaderItemState(
        val title: String,
        override val key: String = title,
    ) : ListItemState()

    data class VaultItemState(
        val vaultListItemState: VaultListItemState,
        override val key: String = vaultListItemState.id,
    ) : ListItemState()
}

data class NewShareItemSelectionData(
    val displayList: List<ListItemState>? = null,
    val query: String? = null,
    val selectedItemCounts: Int = 0,
    val showFooter: Boolean = false,
) : State.View

sealed class NewShareItemSelectionState : State {
    sealed class View : NewShareItemSelectionState(), State.View {
        abstract val data: NewShareItemSelectionData

        data class Initial(override val data: NewShareItemSelectionData) : View()

        data class Loading(override val data: NewShareItemSelectionData) : View()

        data class DisplayItemList(override val data: NewShareItemSelectionData) : View()

        data class ToggleItem(val itemIdToToggle: String, override val data: NewShareItemSelectionData) : View()

        data class UnlockProtectedItems(override val data: NewShareItemSelectionData) : View()
    }

    sealed class SideEffect : NewShareItemSelectionState(), State.SideEffect {
        data object MPLock : SideEffect()

        data class GoToNewShare(
            val selectedPasswords: List<String>,
            val selectedNotes: List<String>,
            val selectedSecrets: List<String>,
        ) : SideEffect()
    }
}

sealed class NewShareItemSelectionEvent : Event {
    data object ViewStarted : NewShareItemSelectionEvent()

    data class ShareableItemsLoaded(val shareableItems: List<ListItemState>, val selectedItemCount: Int) :
        NewShareItemSelectionEvent()

    data class QueryChanged(val query: String) : NewShareItemSelectionEvent()

    data class ItemClicked(val item: VaultListItemState) : NewShareItemSelectionEvent()

    data object NewShareClicked : NewShareItemSelectionEvent()

    data object UnlockRequired : NewShareItemSelectionEvent()

    data object UnlockCancelled : NewShareItemSelectionEvent()

    data class Proceed(
        val selectedPasswords: List<String>,
        val selectedNotes: List<String>,
        val selectedSecrets: List<String>,
    ) : NewShareItemSelectionEvent()
}