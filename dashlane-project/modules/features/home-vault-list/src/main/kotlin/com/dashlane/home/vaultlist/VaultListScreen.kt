package com.dashlane.home.vaultlist

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
            viewModel = hiltViewModel()
        )
    }
}

@Composable
private fun VaultListScreen(viewModel: VaultListViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.viewStarted()
    }

    VaultListContent(
        modifier = Modifier.nestedScroll(rememberNestedScrollInteropConnection()),
        state = state
    )
}