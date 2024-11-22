package com.dashlane.vault.item

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dashlane.design.component.ButtonLayout
import com.dashlane.design.component.ButtonMedium
import com.dashlane.design.component.Icon
import com.dashlane.design.component.ListItem
import com.dashlane.design.component.ListItemActions
import com.dashlane.design.component.Thumbnail
import com.dashlane.design.component.ThumbnailSize
import com.dashlane.design.component.ThumbnailType
import com.dashlane.design.iconography.IconToken
import com.dashlane.design.iconography.IconTokens
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.color.DecorativeColor
import com.dashlane.design.theme.color.Intensity
import com.dashlane.ui.thumbnail.ThumbnailDomainIcon

@Composable
fun VaultListItem(
    item: VaultListItemState,
    modifier: Modifier = Modifier,
    onClick: (String) -> Unit,
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

    val colors = DashlaneTheme.colors
    val title = remember(item.title) { AnnotatedString(item.title) }
    val description = remember(item.description, item.hasError) {
        buildAnnotatedString {
            if (item.hasError) {
                withStyle(SpanStyle(color = colors.textWarningQuiet.value)) {
                    append(item.description)
                }
            } else {
                append(item.description)
            }
        }
    }

    ListItem(
        modifier = modifier,
        title = title,
        description = description,
        titleExtraContent = buildTitleTrailingIcons(
            isPasskeyIconVisible = item.isPasskeyIconVisible,
            isLockIconVisible = item.isLockIconVisible,
            isSharedIconVisible = item.isSharedIconVisible,
            isAttachmentIconVisible = item.isAttachmentIconVisible,
        ),
        thumbnail = buildThumbnail(item.thumbnail),
        actions = actions,
        onClick = { onClick(item.id) },
        onLongClick = onLongClick,
    )
}

private fun buildTitleTrailingIcons(
    isPasskeyIconVisible: Boolean,
    isLockIconVisible: Boolean,
    isSharedIconVisible: Boolean,
    isAttachmentIconVisible: Boolean,
): @Composable (RowScope.() -> Unit) = @Composable {
    if (isPasskeyIconVisible) {
        Icon(
            modifier = Modifier.size(12.dp),
            token = IconTokens.passkeyOutlined,
            contentDescription = stringResource(id = R.string.and_accessibility_vault_item_list_passkey)
        )
    }
    if (isLockIconVisible) {
        Icon(
            modifier = Modifier.size(12.dp),
            token = IconTokens.lockOutlined,
            contentDescription = stringResource(id = R.string.and_accessibility_vault_item_list_locked)
        )
    }
    if (isSharedIconVisible) {
        Icon(
            modifier = Modifier.size(12.dp),
            token = IconTokens.sharedOutlined,
            contentDescription = stringResource(id = R.string.and_accessibility_vault_item_list_shared)
        )
    }
    if (isAttachmentIconVisible) {
        Icon(
            modifier = Modifier.size(12.dp),
            token = IconTokens.attachmentOutlined,
            contentDescription = stringResource(id = R.string.and_accessibility_vault_item_list_has_attachment)
        )
    }
}

private fun buildThumbnail(thumbnail: VaultListItemState.ThumbnailState?): @Composable (() -> Unit) = @Composable {
    when (thumbnail) {
        is VaultListItemState.ThumbnailState.DomainIcon ->
            ThumbnailDomainIcon(
                urlDomain = thumbnail.url,
                size = ThumbnailSize.Medium,
            )

        is VaultListItemState.ThumbnailState.LegacyOtherIcon ->
            Thumbnail(
                type = ThumbnailType.VaultItem.LegacyOtherIcon(
                    token = thumbnail.token,
                    color = thumbnail.color?.let { colorResource(id = it) }
                        ?: DashlaneTheme.colors.containerExpressiveNeutralQuietIdle,
                ),
                size = ThumbnailSize.Medium,
            )

        is VaultListItemState.ThumbnailState.OtherIcon ->
            Thumbnail(
                type = ThumbnailType.VaultItem.OtherIcon(
                    token = thumbnail.token,
                    color = DecorativeColor.GREY,
                ),
                size = ThumbnailSize.Medium,
            )
        null -> {
            
        }
    }
}

@Composable
private fun buildVaultListItemActions(
    isCopyButtonVisible: Boolean,
    isMoreButtonVisible: Boolean,
    onCopyClicked: () -> Unit,
    onMoreClicked: () -> Unit,
): ListItemActions? {
    val copyActionAccessibilityLabel = stringResource(id = R.string.and_accessibility_copy_password)
    val quickActionsAccessibilityLabel = stringResource(id = R.string.and_accessibility_quick_action)

    return remember(isCopyButtonVisible, isMoreButtonVisible) {
        if (!isCopyButtonVisible && !isMoreButtonVisible) {
            return@remember null
        }

        object : ListItemActions {
            @Composable
            override fun Content() {
                if (isCopyButtonVisible) {
                    ButtonMedium(
                        modifier = Modifier
                            .clearAndSetSemantics { },
                        onClick = onCopyClicked,
                        intensity = Intensity.Supershy,
                        layout = ButtonLayout.IconOnly(
                            iconToken = IconTokens.actionCopyOutlined,
                            contentDescription = "", 
                        ),
                    )
                } else {
                    Spacer(modifier = Modifier.width(56.dp))
                }

                if (isMoreButtonVisible) {
                    ButtonMedium(
                        modifier = Modifier
                            .clearAndSetSemantics { },
                        onClick = onMoreClicked,
                        intensity = Intensity.Supershy,
                        layout = ButtonLayout.IconOnly(
                            iconToken = IconTokens.actionMoreOutlined,
                            contentDescription = "", 
                        ),
                    )
                } else {
                    Spacer(modifier = Modifier.width(56.dp))
                }
            }

            override fun getCustomAccessibilityActions(): List<CustomAccessibilityAction> = buildList {
                if (isCopyButtonVisible) {
                    add(
                        CustomAccessibilityAction(
                            label = copyActionAccessibilityLabel,
                            action = {
                                onCopyClicked()
                                true
                            }
                        )
                    )
                }

                if (isMoreButtonVisible) {
                    add(
                        CustomAccessibilityAction(
                            label = quickActionsAccessibilityLabel,
                            action = {
                                onMoreClicked()
                                true
                            }
                        )
                    )
                }
            }
        }
    }
}

data class VaultListItemState(
    val id: String,
    val title: String,
    val description: String,
    val hasError: Boolean = false,
    val isSharedIconVisible: Boolean = false,
    val isAttachmentIconVisible: Boolean = false,
    val isLockIconVisible: Boolean = false,
    val isPasskeyIconVisible: Boolean = false,
    val isCopyButtonVisible: Boolean = false,
    val isMoreButtonVisible: Boolean = false,
    val thumbnail: ThumbnailState? = null,
    val extraContentLoaded: Boolean = false,
) {
    sealed class ThumbnailState {
        data class DomainIcon(val url: String?) : ThumbnailState()
        data class OtherIcon(val token: IconToken) : ThumbnailState()
        data class LegacyOtherIcon(val token: IconToken, val color: Int?) : ThumbnailState()
    }
}

@Preview
@Composable
private fun VaultListItemDomainThumbnailPreview() = DashlaneTheme {
    VaultListItem(
        item = VaultListItemState(
            id = "id",
            title = "Title",
            description = "Description",
            isSharedIconVisible = true,
            isAttachmentIconVisible = true,
            isLockIconVisible = true,
            isPasskeyIconVisible = true,
            isCopyButtonVisible = true,
            isMoreButtonVisible = true,
            thumbnail = VaultListItemState.ThumbnailState.DomainIcon("https://www.dashlane.com"),
        ),
        onClick = {},
    )
}

@Preview
@Composable
private fun VaultListItemLegacyThumbnailPreview() = DashlaneTheme {
    VaultListItem(
        item = VaultListItemState(
            id = "id",
            title = "Title",
            description = "Description",
            thumbnail = VaultListItemState.ThumbnailState.LegacyOtherIcon(
                IconTokens.homeOutlined,
                color = R.color.securenote_color_blue
            ),
        ),
        onClick = {},
    )
}

@Preview
@Composable
private fun VaultListItemIconThumbnailPreview() = DashlaneTheme {
    VaultListItem(
        item = VaultListItemState(
            id = "id",
            title = "Title",
            description = "Description",
            thumbnail = VaultListItemState.ThumbnailState.OtherIcon(IconTokens.homeOutlined),
        ),
        onClick = {},
    )
}