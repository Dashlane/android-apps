package com.dashlane.home.vaultlist

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.ui.common.compose.utils.rememberLargeScreen

@Composable
internal fun VaultListContent(
    modifier: Modifier = Modifier,
    state: VaultListState
) {
    val columnCount = rememberLargeScreen { isLarge ->
        if (isLarge) 2 else 1
    }

    
    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        LazyVerticalGrid(
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
                        modifier = Modifier.padding(16.dp),
                        text = item.title,
                        style = DashlaneTheme.typography.titleSectionLarge
                    )
                    is ListItemState.VaultItemState -> Text(
                        modifier = Modifier.padding(16.dp),
                        text = item.title
                    )
                }
            }
        }
    }
}

@Preview
@Preview(device = Devices.TABLET)
@Composable
private fun VaultListContentPreview() = DashlanePreview {
    VaultListContent(
        state = VaultListState(
            items = (1 until 100)
                .groupBy { it / 10 }
                .flatMap { (key, value) ->
                    listOf(ListItemState.HeaderItemState("Group $key")) +
                        value.map { ListItemState.VaultItemState(it.toString(), "Item $it") }
                }
        )
    )
}