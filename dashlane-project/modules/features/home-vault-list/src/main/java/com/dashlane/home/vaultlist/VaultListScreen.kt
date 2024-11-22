package com.dashlane.home.vaultlist

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dashlane.design.theme.DashlaneTheme

fun ComposeView.setVaultListContent() = setContent {
    DashlaneTheme {
        VaultListScreen(
            viewModel = hiltViewModel(),
        )
    }
}

@Composable
private fun VaultListScreen(
    viewModel: VaultListViewModel,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    VaultListContent(
        modifier = Modifier.nestedScroll(rememberNestedScrollInteropConnection()),
        state = state,
        onRefresh = viewModel::refresh,
        onItemClick = viewModel::onItemClick,
        onLongClick = viewModel::onLongClick,
        onCopyClicked = viewModel::onCopyClicked,
        onMoreClicked = viewModel::onMoreClicked,
        loadItemExtraContent = viewModel::loadItemExtraContent,
    )
}