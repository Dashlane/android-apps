package com.dashlane.search

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.dashlane.design.theme.typography.withSearchHighlight
import com.dashlane.vault.item.BaseVaultListItem
import com.dashlane.vault.item.VaultListItemState
import com.dashlane.vault.item.buildThumbnail
import com.dashlane.vault.item.buildTitleTrailingIcons
import com.dashlane.vault.item.buildVaultListItemActions

@Composable
fun SearchVaultListItem(
    item: SearchVaultListItemState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: () -> Unit = {},
    onCopyClicked: () -> Unit = {},
    onMoreClicked: () -> Unit = {},
) {
    val actions = buildVaultListItemActions(
        isCopyButtonVisible = item.isCopyButtonVisible,
        isMoreButtonVisible = item.isMoreButtonVisible,
        onCopyClicked = onCopyClicked,
        onMoreClicked = onMoreClicked,
    )

    BaseVaultListItem(
        modifier = modifier,
        title = item.title.withSearchHighlight(
            match = item.highlightText,
            ignoreCase = true,
        ),
        description = item.description.withSearchHighlight(
            match = item.highlightText,
            ignoreCase = true,
        ),
        titleExtraContent = buildTitleTrailingIcons(
            isPasskeyIconVisible = item.isPasskeyIconVisible,
            isLockIconVisible = item.isLockIconVisible,
            isSharedIconVisible = item.isSharedIconVisible,
            isAttachmentIconVisible = item.isAttachmentIconVisible,
        ),
        thumbnail = buildThumbnail(item.thumbnail),
        actions = actions,
        onClick = onClick,
        onLongClick = onLongClick,
    )
}

data class SearchVaultListItemState(
    val itemId: String,
    val title: String,
    val description: String,
    val highlightText: String?,
    val hasError: Boolean = false,
    val isSharedIconVisible: Boolean = false,
    val isAttachmentIconVisible: Boolean = false,
    val isLockIconVisible: Boolean = false,
    val isPasskeyIconVisible: Boolean = false,
    val isCopyButtonVisible: Boolean = false,
    val isMoreButtonVisible: Boolean = false,
    val thumbnail: VaultListItemState.ThumbnailState? = null,
    val extraContentLoaded: Boolean = false,
)