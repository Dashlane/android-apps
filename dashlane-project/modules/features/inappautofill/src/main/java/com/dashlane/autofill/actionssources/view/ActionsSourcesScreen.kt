package com.dashlane.autofill.actionssources.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dashlane.autofill.api.R
import com.dashlane.design.component.ButtonLayout
import com.dashlane.design.component.ButtonMedium
import com.dashlane.design.component.DashlaneSnackbarWrapper
import com.dashlane.design.component.IndeterminateLoader
import com.dashlane.design.component.SnackbarType
import com.dashlane.design.iconography.IconTokens
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.color.Mood
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.ui.common.compose.components.EmptyContent
import com.dashlane.ui.common.compose.components.SimpleStateContent
import com.dashlane.ui.common.compose.components.basescreen.scaffoldBackgroundAndInsets
import com.dashlane.ui.common.compose.components.components.DashlaneTopAppBar
import kotlinx.coroutines.launch

@Suppress("LongMethod")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ActionsSourcesScreen(
    viewModel: ActionsSourcesViewModel,
    onNavigateUp: () -> Unit,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val lifecycleState by lifecycleOwner.lifecycle.currentStateFlow.collectAsStateWithLifecycle()
    LaunchedEffect(lifecycleState) {
        when (lifecycleState) {
            Lifecycle.State.RESUMED -> viewModel.onRefresh()
            else -> Unit
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    LaunchedEffect(viewModel) {
        viewModel.stateFlow.sideEffect.collect { sideEffect ->
            when (sideEffect) {
                is ActionsSourcesState.SideEffect.OnServiceResumed -> {
                    scope.launch {
                        snackbarHostState.showSnackbar(message = sideEffect.message)
                    }
                }
            }
        }
    }

    val uiState by viewModel.stateFlow.viewState.collectAsStateWithLifecycle()
    Scaffold(
        modifier = Modifier.scaffoldBackgroundAndInsets(),
        containerColor = DashlaneTheme.colors.backgroundAlternate,
        topBar = {
            DashlaneTopAppBar(
                text = stringResource(R.string.autofill_actioned_sources_paused_title),
                navigationAction = DashlaneTopAppBar.NavigationAction(
                    iconToken = IconTokens.arrowLeftOutlined,
                    contentDescription = stringResource(R.string.and_accessibility_back_button),
                    onClick = onNavigateUp,
                ),
            )
        },
        snackbarHost = {
            DashlaneSnackbarWrapper(type = SnackbarType.STANDARD) {
                SnackbarHost(hostState = snackbarHostState)
            }
        },
    ) { contentPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
            contentAlignment = Alignment.TopCenter,
        ) {
            when (val viewState = uiState) {
                is ActionsSourcesState.View.Initial -> {
                    Loader()
                }
                is ActionsSourcesState.View.EmptyState -> {
                    ActionsSourcesEmptyContent()
                }
                is ActionsSourcesState.View.Success -> {
                    ActionsSourcesListContent(
                        temporarilyPausedItems = viewState.data.temporarilyPausedItems,
                        permanentlyPausedItems = viewState.data.permanentlyPausedItems,
                        onResumeClick = viewModel::onResumeClick,
                    )
                }
                is ActionsSourcesState.View.Error -> {
                    ActionsSourcesErrorContent(
                        onRetryClick = viewModel::onRefresh,
                    )
                }
            }
        }
    }
}

@Composable
private fun BoxScope.Loader() {
    val contentDescription = stringResource(R.string.and_accessibility_paused_autofill_setting_loading)
    IndeterminateLoader(
        modifier = Modifier
            .semantics {
                this.contentDescription = contentDescription
            }
            .align(Alignment.Center),
    )
}

@Composable
internal fun ActionsSourcesEmptyContent() {
    EmptyContent(
        iconToken = IconTokens.featureAutofillOutlined,
        title = stringResource(R.string.autofill_actioned_sources_items_empty_title),
        description = stringResource(R.string.autofill_actioned_sources_items_empty_description),
    )
}

@Preview
@Composable
private fun ActionsSourcesEmptyContentPreview() {
    DashlanePreview {
        ActionsSourcesEmptyContent()
    }
}

@Composable
private fun ActionsSourcesErrorContent(onRetryClick: () -> Unit) {
    SimpleStateContent(
        iconToken = IconTokens.feedbackFailOutlined,
        iconMood = Mood.Danger,
        title = stringResource(R.string.autofill_list_items_error_title),
        description = stringResource(R.string.autofill_list_items_error_description),
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        ButtonMedium(
            layout = ButtonLayout.TextOnly(stringResource(R.string.autofill_list_items_error_retry_action)),
            onClick = onRetryClick,
        )
    }
}

@Preview
@Composable
private fun ActionsSourcesErrorContentPreview() {
    DashlanePreview {
        ActionsSourcesErrorContent(
            onRetryClick = {},
        )
    }
}