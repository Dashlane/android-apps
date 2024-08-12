package com.dashlane.item.v3.display

import androidx.appcompat.app.ActionBar
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.ComposeView
import com.dashlane.item.v3.util.setupToolbar
import com.dashlane.item.v3.viewmodels.State

@Composable
internal fun Toolbar(
    actionBar: ActionBar?,
    lazyListState: LazyListState,
    uiState: State,
    actionBarView: ComposeView
) {
    val firstVisibleIndex by remember { derivedStateOf { lazyListState.firstVisibleItemIndex } }
    val isNameVisible = firstVisibleIndex > 0 || uiState.isEditMode
    actionBarView.setupToolbar(uiState, isNameVisible)
    if (actionBarView.parent == null) {
        actionBar?.customView = actionBarView
    }
}
