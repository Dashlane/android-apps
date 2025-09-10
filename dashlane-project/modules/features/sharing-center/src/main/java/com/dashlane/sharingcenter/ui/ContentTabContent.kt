package com.dashlane.sharingcenter.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dashlane.design.component.cardBackground
import com.dashlane.design.iconography.IconTokens
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.sharing_center.R
import com.dashlane.sharingcenter.SharedItemData
import com.dashlane.sharingcenter.fabBottomPadding
import com.dashlane.sharingcenter.fabSize
import com.dashlane.ui.common.compose.components.EmptyContent
import com.dashlane.vault.item.VaultListItem
import com.dashlane.vault.item.VaultListItemState

@Suppress("LongMethod")
@Composable
internal fun ContentTabContent(
    collections: List<VaultListItemState>,
    items: List<SharedItemData>,
    onCollectionClicked: (id: String) -> Unit,
    onItemClicked: (id: String, itemType: String) -> Unit,
) {
    if (collections.isEmpty() && items.isEmpty()) {
        EmptyContent(
            iconToken = IconTokens.vaultOutlined,
            title = stringResource(R.string.sharing_center_tab_content_empty_title),
            description = stringResource(R.string.sharing_center_tab_content_empty_description),
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                
                top = 0.dp,
                
                bottom = 16.dp + fabSize + fabBottomPadding,
                start = 16.dp,
                end = 16.dp,
            ),
        ) {
            if (collections.isNotEmpty()) {
                item(key = "Header-content-collection-section") {
                    SectionHeader(
                        modifier = Modifier.padding(top = 16.dp),
                        title = stringResource(R.string.sharing_center_content_collections_header),
                    )
                }
                items(
                    items = collections,
                    key = { collection -> "content collectionID - ${collection.id}" },
                ) { collection ->
                    VaultListItem(
                        modifier = Modifier
                            .animateItem()
                            .fillMaxWidth()
                            .cardBackground(isTop = false, isBottom = false)
                            .padding(
                                start = 8.dp,
                                end = 8.dp,
                            ),
                        item = collection,
                        onClick = {
                            onCollectionClicked(collection.id)
                        },
                    )
                }
                item(key = "content-collection-section-card-bottom") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .cardBackground(isTop = false, isBottom = true),
                    )
                }
            }
            if (items.isNotEmpty()) {
                item(key = "Header-content-items-section") {
                    SectionHeader(
                        modifier = Modifier.padding(top = 16.dp),
                        title = stringResource(R.string.sharing_center_content_items_header),
                    )
                }
                items(
                    items = items,
                    key = { item -> "content item - ${item.vaultListItemState.id}" },
                ) { item ->
                    VaultListItem(
                        modifier = Modifier
                            .animateItem()
                            .fillMaxWidth()
                            .cardBackground(isTop = false, isBottom = false)
                            .padding(
                                start = 8.dp,
                                end = 8.dp,
                            ),
                        item = item.vaultListItemState,
                        onClick = {
                            onItemClicked(item.vaultListItemState.id, item.itemType)
                        },
                    )
                }
                item(key = "content-items-section-card-bottom") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .cardBackground(isTop = false, isBottom = true),
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun ContentTabContentEmptyPreview() {
    DashlanePreview {
        ContentTabContent(
            collections = emptyList(),
            items = emptyList(),
            onCollectionClicked = {},
            onItemClicked = { _, _ -> },
        )
    }
}