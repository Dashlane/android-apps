package com.dashlane.home.vaultlist

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dashlane.design.component.IndeterminateLoader
import com.dashlane.design.component.Text
import com.dashlane.design.iconography.IconTokens
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.ui.adapter.ItemListContext
import com.dashlane.ui.common.compose.components.EmptyScreen
import com.dashlane.ui.common.compose.utils.rememberLargeScreen
import com.dashlane.vault.item.VaultListItem
import com.dashlane.vault.item.VaultListItemState
import com.dashlane.vault.summary.SummaryObject
import my.nanihadesuka.compose.LazyVerticalGridScrollbar
import my.nanihadesuka.compose.ScrollbarSettings

@Suppress("LongMethod")
@OptIn(ExperimentalMaterialApi::class)
@Composable
internal fun VaultListContent(
    modifier: Modifier = Modifier,
    state: VaultListState,
    onRefresh: () -> Unit,
    onItemClick: (String) -> Unit,
    onLongClick: (String, ItemListContext) -> Unit,
    onCopyClicked: (String, ItemListContext) -> Unit,
    onMoreClicked: (String, ItemListContext) -> Unit,
    loadItemExtraContent: (SummaryObject) -> Unit,
) {
    val columnCount = rememberLargeScreen { isLarge ->
        if (isLarge) 2 else 1
    }

    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        val pullRefreshState = rememberPullRefreshState(
            refreshing = state.isRefreshing,
            onRefresh = onRefresh
        )
        val lazyGridState = rememberLazyGridState()

        LazyVerticalGridScrollbar(
            state = lazyGridState,
            settings = ScrollbarSettings(
                scrollbarPadding = 0.dp,
                thumbThickness = 8.dp,
                thumbShape = RectangleShape,
                thumbUnselectedColor = DashlaneTheme.colors.borderNeutralStandardIdle,
                thumbSelectedColor = DashlaneTheme.colors.borderNeutralStandardIdle,
                hideDelayMillis = 1500,
                ),
        ) {
            LazyVerticalGrid(
                modifier = Modifier
                    .fillMaxSize()
                    .pullRefresh(pullRefreshState),
                state = lazyGridState,
                columns = GridCells.Fixed(count = columnCount),
                contentPadding = PaddingValues(bottom = 64.dp),
            ) {
                items(
                    items = state.items,
                    key = { it.key },
                    span = { item ->
                        when (item) {
                            is ListItemState.HeaderItemState -> GridItemSpan(columnCount)
                            is ListItemState.VaultItemState -> GridItemSpan(1)
                        }
                    },
                ) { item ->
                    when (item) {
                        is ListItemState.HeaderItemState -> Text(
                            modifier = Modifier.padding(
                                top = 24.dp,
                                bottom = 8.dp,
                                start = 16.dp,
                                end = 16.dp,
                            ),
                            text = item.title,
                            style = DashlaneTheme.typography.titleSupportingSmall,
                            color = DashlaneTheme.colors.textNeutralQuiet,
                            enforceAllCaps = true
                        )
                        is ListItemState.VaultItemState -> {
                            LaunchedEffect(item.key, item.vaultItemState.extraContentLoaded) {
                                if (!item.vaultItemState.extraContentLoaded) {
                                    item.summaryObject?.let { loadItemExtraContent(it) }
                                }
                            }

                            VaultListItem(
                                modifier = Modifier.padding(horizontal = 8.dp),
                                item = item.vaultItemState,
                                onClick = { onItemClick(item.vaultItemState.id) },
                                onLongClick = { onLongClick(item.vaultItemState.id, item.itemListContext) },
                                onCopyClicked = { onCopyClicked(item.vaultItemState.id, item.itemListContext) },
                                onMoreClicked = { onMoreClicked(item.vaultItemState.id, item.itemListContext) },
                            )
                        }
                    }
                }
            }
        }

        if (state.emptyState != null) {
            EmptyScreen(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = (-96).dp),
                icon = state.emptyState.icon,
                title = state.emptyState.title,
                description = state.emptyState.description,
            )
        }

        if (state.isLoading) {
            IndeterminateLoader(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = (-96).dp),
            )
        }

        PullRefreshIndicator(
            refreshing = state.isRefreshing,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter),
            backgroundColor = DashlaneTheme.colors.backgroundAlternate,
            contentColor = DashlaneTheme.colors.borderBrandStandardActive
        )
    }
}

@Preview
@Composable
private fun VaultListLoadingPreview() = DashlanePreview {
    VaultListContent(
        state = VaultListState(
            isLoading = true,
        ),
        onRefresh = {},
        onItemClick = {},
        onLongClick = { _, _ -> },
        onCopyClicked = { _, _ -> },
        onMoreClicked = { _, _ -> },
        loadItemExtraContent = {},
    )
}

@Preview
@Composable
private fun VaultListContentPreview() = DashlanePreview {
    VaultListContent(
        state = VaultListState(
            items = (1 until 100)
                .groupBy { it / 10 }
                .flatMap { (key, value) ->
                    listOf(ListItemState.HeaderItemState(key.toString())) +
                        value.map {
                            ListItemState.VaultItemState(
                                key = "item $it",
                                vaultItemState = VaultListItemState(
                                    id = "id $it",
                                    title = "Title $it",
                                    description = "Description $it",
                                    thumbnail = VaultListItemState.ThumbnailState.DomainIcon(""),
                                ),
                                itemListContext = ItemListContext(),
                                summaryObject = null,
                            )
                        }
                }
        ),
        onRefresh = {},
        onItemClick = {},
        onLongClick = { _, _ -> },
        onCopyClicked = { _, _ -> },
        onMoreClicked = { _, _ -> },
        loadItemExtraContent = {},
    )
}

@Preview
@Composable
private fun VaultListContentEmptyPreview() = DashlanePreview {
    VaultListContent(
        state = VaultListState(
            items = emptyList(),
            emptyState = VaultListEmptyState(
                icon = IconTokens.protectionOutlined,
                title = "One secure place for all your important info",
                description = "Add anything from logins to payments—and so much more!",
            )
        ),
        onRefresh = {},
        onItemClick = {},
        onLongClick = { _, _ -> },
        onCopyClicked = { _, _ -> },
        onMoreClicked = { _, _ -> },
        loadItemExtraContent = {},
    )
}