package com.dashlane.search

import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dashlane.design.component.IndeterminateLoader
import com.dashlane.design.component.Text
import com.dashlane.design.iconography.IconTokens
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.search.fab.SearchFabState
import com.dashlane.search.fab.SearchFabViewModel
import com.dashlane.ui.adapter.ItemListContext
import com.dashlane.ui.common.compose.components.EmptyContent
import com.dashlane.ui.common.compose.components.fab.MultiActionsFloatingActionButton
import com.dashlane.vault.item.VaultListItemState
import com.dashlane.vault.model.createAuthentifiant
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.summary.toSummary
import com.dashlane.xml.domain.SyncObjectType

@Composable
@Suppress("LongMethod")
fun SearchScreen(
    searchViewModel: SearchViewModel,
    fabViewModel: SearchFabViewModel,
    openItemDetails: (String, SyncObjectType) -> Unit,
    openQuickActions: (String, ItemListContext) -> Unit,
    onAddItemClick: (SyncObjectType) -> Unit,
    onResultDisplayed: (SearchRequest, Int) -> Unit,
) {
    LaunchedEffect(searchViewModel) {
        searchViewModel.searchState.sideEffect.collect { sideEffect ->
            when (sideEffect) {
                is SearchState.SideEffect.OpenQuickActions -> openQuickActions(
                    sideEffect.itemId,
                    sideEffect.itemListContext,
                )

                is SearchState.SideEffect.OpenItemDetails -> openItemDetails(
                    sideEffect.itemId,
                    sideEffect.itemType,
                )
            }
        }
    }

    LaunchedEffect(fabViewModel) {
        fabViewModel.fabState.sideEffect.collect { sideEffect ->
            when (sideEffect) {
                is SearchFabState.SideEffect.CreateItem -> onAddItemClick(sideEffect.itemType)
            }
        }
    }

    val viewState by searchViewModel.searchState.viewState.collectAsStateWithLifecycle()
    val fabViewState by fabViewModel.fabState.viewState.collectAsStateWithLifecycle()

    LaunchedEffect(viewState) {
        fabViewModel.close()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (val state = viewState) {
            is SearchState.ViewState.EmptyState -> {
                EmptySearchContent()
            }

            is SearchState.ViewState.SearchResult -> {
                LaunchedEffect(state) {
                    onResultDisplayed(viewState.searchRequest, viewState.items.size)
                }
                SearchScreenContent(
                    viewState = state,
                    onVaultItemClick = searchViewModel::onVaultItemClick,
                    onMoreActionClick = searchViewModel::onMoreActionClick,
                    onCopyActionClick = searchViewModel::onCopyActionClick,
                    loadItemExtraContent = searchViewModel::loadItemExtraContent,
                    contentPadding = PaddingValues(16.dp),
                )
            }
        }
        if (viewState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                IndeterminateLoader()
            }
        }
        SearchFab(fabViewModel = fabViewModel, fabViewState = fabViewState, viewState = viewState)
    }
}

@Composable
private fun SearchFab(
    fabViewModel: SearchFabViewModel,
    fabViewState: SearchFabState.View,
    viewState: SearchState.ViewState,
) {
    Scrim(
        color = DashlaneTheme.colors.oddityOverlay,
        onDismissRequest = fabViewModel::close,
        visible = fabViewState.isExpanded,
    )
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.BottomEnd,
    ) {
        if (viewState is SearchState.ViewState.EmptyState) {
            MultiActionsFloatingActionButton(
                modifier = Modifier.padding(16.dp),
                fabMenu = fabViewState.fabContent,
                onPrimaryFabClick = fabViewModel::onPrimaryFabClick,
                onActionFabClick = fabViewModel::onActionClick,
                isExpanded = fabViewState.isExpanded,
            )
        }
    }
}

@Composable
private fun Scrim(
    color: Color,
    onDismissRequest: () -> Unit,
    visible: Boolean,
) {
    if (color.isSpecified) {
        val alpha by animateFloatAsState(
            targetValue = if (visible) 1f else 0f,
            animationSpec = TweenSpec(),
        )
        val dismissSheet =
            if (visible) {
                Modifier
                    .pointerInput(onDismissRequest) { detectTapGestures { onDismissRequest() } }
                    .semantics(mergeDescendants = true) {
                        traversalIndex = 1f
                        contentDescription = ""
                        onClick {
                            onDismissRequest()
                            true
                        }
                    }
            } else {
                Modifier
            }
        Canvas(
            Modifier
                .fillMaxSize()
                .then(dismissSheet),
        ) {
            drawRect(color = color, alpha = alpha.coerceIn(0f, 1f))
        }
    }
}

@Composable
private fun EmptySearchContent() {
    EmptyContent(
        iconToken = IconTokens.feedbackFailOutlined,
        title = stringResource(R.string.search_screen_empty_state_body),
        description = null,
    )
}

@Composable
@SuppressWarnings("kotlin:S107") 
private fun SearchScreenContent(
    viewState: SearchState.ViewState.SearchResult,
    onVaultItemClick: (SearchListItem.Item.Vault) -> Unit,
    onMoreActionClick: (SearchListItem.Item.Vault, ItemListContext) -> Unit,
    onCopyActionClick: (SearchListItem.Item.Vault) -> Unit,
    loadItemExtraContent: (SummaryObject) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize(),
        contentPadding = contentPadding,
    ) {
        val results = viewState.items
        items(
            items = results,
            key = { it.hashCode() },
        ) { item ->
            when (item) {
                is SearchListItem.Header -> Text(
                    modifier = Modifier
                        .animateItem()
                        .padding(
                            top = 8.dp,
                            bottom = 8.dp,
                            start = 16.dp,
                            end = 16.dp,
                        ),
                    text = item.text,
                    style = DashlaneTheme.typography.titleSupportingSmall,
                    color = DashlaneTheme.colors.textNeutralQuiet,
                    enforceAllCaps = true,
                )

                is SearchListItem.Item.Vault -> {
                    LaunchedEffect(item.itemState.itemId, item.itemState.extraContentLoaded) {
                        if (!item.itemState.extraContentLoaded) {
                            loadItemExtraContent(item.summaryObject)
                        }
                    }
                    SearchVaultListItem(
                        modifier = Modifier.animateItem(),
                        item = item.itemState,
                        onClick = { onVaultItemClick(item) },
                        onLongClick = { onMoreActionClick(item, item.itemListContext) },
                        onCopyClicked = { onCopyActionClick(item) },
                        onMoreClicked = { onMoreActionClick(item, item.itemListContext) },
                    )
                }
            }
        }
    }
}

@Composable
@Preview
private fun SearchScreenContentPreview() {
    DashlanePreview {
        SearchScreenContent(
            viewState = SearchState.ViewState.SearchResult(
                searchRequest = SearchRequest.FromQuery("dash"),
                isLoading = false,
                items = listOf<SearchListItem>(
                    SearchListItem.Item.Vault(
                        itemState = SearchVaultListItemState(
                            itemId = "id",
                            title = "Dashlane",
                            description = "Did you know? Impalas can dash pretty fast.",
                            highlightText = "dash",
                            thumbnail = VaultListItemState.ThumbnailState.VaultThumbnail.DomainIcon(url = "dashlane.com"),
                        ),
                        itemListContext = ItemListContext(),
                        summaryObject = createAuthentifiant(
                            itemId = "id",
                            title = "Dashlane",
                        ).toSummary(),
                    ),
                ),
            ),
            onVaultItemClick = {},
            onMoreActionClick = { _, _ -> },
            onCopyActionClick = {},
            loadItemExtraContent = {},
        )
    }
}

@Composable
@Preview
private fun SearchScreenContentEmptyStatePreview() {
    DashlanePreview {
        EmptySearchContent()
    }
}