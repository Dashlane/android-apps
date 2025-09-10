package com.dashlane.ui.screens.fragments.userdata.sharing.itemselection.old

import com.dashlane.mvvm.State
import com.dashlane.vault.summary.SummaryObject

class NewShareItemUIState {
    data class View(
        val list: List<SummaryObject>? = null,
        val query: String? = null,
        val selectionForSharing: List<SummaryObject> = emptyList(),
        val showFab: Boolean = false,
    ) : State.View

    sealed class SideEffect : State.SideEffect {
        data object MPLock : SideEffect()
    }
}