package com.dashlane.collections.details

import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.MenuItem
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dashlane.R
import com.dashlane.collections.CollectionLoading
import com.dashlane.collections.DeleteConfirmationDialog
import com.dashlane.collections.RevokeToDeleteDialog
import com.dashlane.design.component.ButtonLayout
import com.dashlane.design.component.Dialog
import com.dashlane.design.component.DropdownItem
import com.dashlane.design.component.DropdownMenu
import com.dashlane.design.component.Icon
import com.dashlane.design.component.InfoboxMedium
import com.dashlane.design.component.Text
import com.dashlane.design.component.Thumbnail
import com.dashlane.design.component.ThumbnailType
import com.dashlane.design.component.cardBackground
import com.dashlane.design.iconography.IconTokens
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.color.Mood
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.teamspaces.model.SpaceColor
import com.dashlane.ui.thumbnail.ThumbnailDomainIcon
import com.dashlane.ui.widgets.compose.OutlinedTeamspaceIcon
import com.dashlane.util.SnackbarUtils
import com.dashlane.util.getBaseActivity
import com.dashlane.util.model.UserPermission
import com.dashlane.vault.model.getColorId
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.domain.SyncObjectXmlName

@Composable
fun CollectionDetailsScreen(
    uiState: ViewState,
    onDismissDialog: () -> Unit,
    onDeleteConfirmed: () -> Unit,
    onRevokeToDeleteClicked: () -> Unit,
    onBack: () -> Unit,
    goToItem: (String, String) -> Unit,
    removeItem: (String, Boolean) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (uiState.viewData.spaceData?.businessSpace == true) {
            InfoBoxSharingLimit(uiState.viewData.collectionLimit)
        }
        when (uiState) {
            is ViewState.List,
            is ViewState.DeletePrompt,
            is ViewState.SharedCollectionDeleteError,
            is ViewState.RevokeAccessPrompt,
            is ViewState.SharingWithAttachmentError -> {
                val context = LocalContext.current
                LaunchedEffect(key1 = uiState) {
                    if (uiState !is ViewState.SharedCollectionDeleteError) return@LaunchedEffect
                    context.getBaseActivity()?.let { activity ->
                        SnackbarUtils.showSnackbar(
                            activity,
                            context.getString(R.string.collection_delete_shared_error_generic)
                        )
                    }
                }
                ItemsList(
                    viewData = uiState.viewData,
                    showRevokePrompt = uiState is ViewState.RevokeAccessPrompt,
                    showDeletePrompt = uiState is ViewState.DeletePrompt,
                    onDismissDialog = onDismissDialog,
                    onDeleteConfirmed = onDeleteConfirmed,
                    onRevokeToDeleteClicked = onRevokeToDeleteClicked,
                    goToItem = goToItem,
                    removeItem = removeItem
                )
            }
            is ViewState.Empty -> EmptyState()
            is ViewState.Deleted -> onBack()
            is ViewState.Loading -> CollectionLoading()
        }
    }
}

@Composable
private fun InfoBoxSharingLimit(collectionLimit: CollectionLimiter.UserLimit) {
    val (mode, title, description) = when (collectionLimit) {
        CollectionLimiter.UserLimit.APPROACHING_LIMIT -> Triple(
            Mood.Brand,
            R.string.collections_limiter_approaching_title,
            R.string.collections_limiter_approaching_description
        )
        CollectionLimiter.UserLimit.REACHED_LIMIT -> Triple(
            Mood.Warning,
            R.string.collections_limiter_reached_title,
            R.string.collections_limiter_right_limit_description
        )
        CollectionLimiter.UserLimit.REACHED_LIMIT_EXISTING_ITEM -> Triple(
            Mood.Warning,
            R.string.collections_limiter_reached_title,
            R.string.collections_limiter_right_limit_existing_item_description
        )
        CollectionLimiter.UserLimit.BUSINESS_TRIAL -> Triple(
            Mood.Brand,
            R.string.collections_limiter_business_trial_title,
            R.string.collections_limiter_business_trial_description
        )
        else -> return
    }
    InfoboxMedium(
        modifier = Modifier.padding(
            start = 16.dp,
            top = 16.dp,
            end = 12.dp,
            bottom = 8.dp
        ),
        mood = mode,
        title = stringResource(id = title),
        description = stringResource(id = description),
    )
}

fun MenuItem.updateStateAndTitleColor(
    enabled: Boolean,
    disabledColor: androidx.compose.ui.graphics.Color
) {
    isEnabled = enabled
    val coloredTitle = if (!isEnabled) {
        val disabledTextColor = disabledColor.toArgb()
        SpannableString(title).apply {
            setSpan(ForegroundColorSpan(disabledTextColor), 0, length, 0)
        }
    } else {
        title
    }
    title = coloredTitle
}

@Composable
fun CollectionDetailScreenToolbar(uiState: ViewState) {
    DashlaneTheme {
        Row {
            Text(
                modifier = Modifier
                    .weight(weight = 1f, fill = false)
                    .semantics { heading() },
                text = uiState.viewData.collectionName ?: "",
                style = DashlaneTheme.typography.titleSectionMedium,
                color = DashlaneTheme.colors.textNeutralCatchy,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            val spaceData = uiState.viewData.spaceData
            if (spaceData != null) {
                Spacer(modifier = Modifier.width(6.dp))
                OutlinedTeamspaceIcon(
                    letter = spaceData.spaceLetter,
                    color = when (val color = spaceData.spaceColor) {
                        is SpaceColor.FixColor -> colorResource(color.colorRes).toArgb()
                        is SpaceColor.TeamColor -> color.color
                    },
                    iconSize = 12.dp,
                    modifier = Modifier.padding(top = 5.dp)
                )
            }

            if (uiState.viewData.shared) {
                Spacer(modifier = Modifier.width(2.dp))
                Icon(
                    token = IconTokens.sharedOutlined,
                    modifier = Modifier
                        .size(16.dp)
                        .padding(top = 6.dp),
                    contentDescription = stringResource(R.string.and_accessibility_collection_list_item_shared)
                )
            }
        }
    }
}

@Composable
private fun ColumnScope.EmptyState() {
    Column(
        modifier = Modifier
            .padding(32.dp)
            .weight(1f),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            modifier = Modifier
                .size(96.dp)
                .focusable(false),
            painter = painterResource(IconTokens.folderOutlined.resource),
            colorFilter = ColorFilter.tint(DashlaneTheme.colors.textNeutralQuiet.value),
            contentDescription = null
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(id = R.string.collection_details_empty_state_description),
            style = DashlaneTheme.typography.bodyStandardRegular,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ItemsList(
    viewData: ViewData,
    showRevokePrompt: Boolean,
    showDeletePrompt: Boolean,
    onDismissDialog: () -> Unit,
    onDeleteConfirmed: () -> Unit,
    onRevokeToDeleteClicked: () -> Unit,
    goToItem: (String, String) -> Unit,
    removeItem: (String, Boolean) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        viewData.sections.forEach { section ->
            item {
                Header(title = section.sectionType.getText())
            }
            items(section.items) { item ->
                VaultItemView(
                    item,
                    viewData.shared,
                    goToItem,
                    removeItem
                )
            }
            item {
                BottomBackground()
            }
        }
    }
    if (showDeletePrompt) {
        DeleteConfirmationDialog(
            onDismiss = {
                onDismissDialog()
            },
            onConfirm = {
                onDeleteConfirmed()
            }
        )
    }
    if (showRevokePrompt) {
        RevokeToDeleteDialog(
            onDismiss = {
                onDismissDialog()
            },
            onConfirm = {
                onRevokeToDeleteClicked()
            }
        )
    }
}

@Composable
private fun Header(title: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 0.dp)
            .cardBackground(isTop = true)
            .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 8.dp),
    ) {
        Text(
            text = title,
            color = DashlaneTheme.colors.textNeutralCatchy,
            style = DashlaneTheme.typography.titleBlockMedium
        )
    }
}

@Composable
private fun BottomBackground() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 0.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)
            .cardBackground(isBottom = true)
            .height(16.dp),
    )
}

@Composable
private fun VaultItemView(
    item: SummaryForUi,
    shared: Boolean,
    goToItem: (String, String) -> Unit,
    removeItem: (String, Boolean) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    VaultItemView(
        item = item,
        shared = shared,
        expandMenu = expanded,
        onExpandMenuChange = { expanded = it },
        onRemoveItemClicked = removeItem,
        goToItem = goToItem,
    )
}

@Suppress("LongMethod")
@Composable
private fun VaultItemView(
    item: SummaryForUi,
    shared: Boolean,
    expandMenu: Boolean,
    onExpandMenuChange: (Boolean) -> Unit,
    onRemoveItemClicked: (String, Boolean) -> Unit,
    goToItem: (String, String) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { goToItem(item.id, item.type) }
            .padding(horizontal = 16.dp)
            .cardBackground(isTop = false, isBottom = false)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { goToItem(item.id, item.type) }
                .padding(vertical = 8.dp)
                .padding(start = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            VaultItemThumbnailView(item)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(weight = 1f, fill = true)) {
                VaultItemTitle(item, shared)
                Text(
                    text = item.secondLine.getText(),
                    style = DashlaneTheme.typography.bodyReducedRegular,
                    color = DashlaneTheme.colors.textNeutralQuiet,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                VaultItemSharedPermission(item, shared)
            }
            Box {
                DropdownMenu(
                    expanded = expandMenu,
                    onDismissRequest = {
                        onExpandMenuChange(false)
                    },
                    anchor = {
                        IconButton(onClick = { onExpandMenuChange(true) }) {
                            Icon(token = IconTokens.actionMoreOutlined, contentDescription = null)
                        }
                    },
                ) {
                    DropdownItem(
                        icon = null,
                        text = stringResource(id = R.string.collection_details_item_menu_remove),
                        onClick = {
                            onExpandMenuChange(false)
                            onRemoveItemClicked(item.id, shared)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun VaultItemThumbnailView(
    item: SummaryForUi,
) {
    when (item.thumbnail) {
        is ThumbnailData.UrlThumbnail -> {
            ThumbnailDomainIcon(
                urlDomain = item.thumbnail.url
            )
        }
        is ThumbnailData.SecureNoteThumbnail -> {
            Thumbnail(
                type = ThumbnailType.VaultItem.LegacyOtherIcon(
                    token = IconTokens.itemSecureNoteOutlined,
                    color = colorResource(id = item.thumbnail.secureNoteType.getColorId()),
                )
            )
        }
    }
}

@Composable
private fun VaultItemTitle(
    item: SummaryForUi,
    shared: Boolean
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            modifier = Modifier.weight(weight = 1f, fill = false),
            text = item.firstLine,
            style = DashlaneTheme.typography.bodyStandardRegular,
            color = DashlaneTheme.colors.textNeutralCatchy,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        if (item.spaceData != null) {
            Spacer(modifier = Modifier.width(6.dp))
            OutlinedTeamspaceIcon(
                letter = item.spaceData.spaceLetter,
                color = when (val color = item.spaceData.spaceColor) {
                    is SpaceColor.FixColor -> colorResource(color.colorRes).toArgb()
                    is SpaceColor.TeamColor -> color.color
                },
                iconSize = 12.dp,
                teamspaceDescription = stringResource(id = item.spaceData.spaceContentDescriptionResId)
            )
        }

        if (shared) {
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                token = IconTokens.sharedOutlined,
                modifier = Modifier.size(12.dp),
                contentDescription = stringResource(R.string.and_accessibility_collection_list_item_shared)
            )
        }
    }
}

@Composable
private fun VaultItemSharedPermission(item: SummaryForUi, shared: Boolean) {
    if (shared && item.sharingPermission != null) {
        val rights = if (item.sharingPermission == UserPermission.LIMITED) {
            stringResource(id = R.string.enum_sharing_permission_limited)
        } else {
            stringResource(id = R.string.enum_sharing_permission_admin)
        }
        Text(
            text = rights,
            style = DashlaneTheme.typography.bodyReducedRegular,
            color = DashlaneTheme.colors.textNeutralQuiet,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun DialogBusinessMemberLimit(
    onDismissRequest: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        mainActionClick = onDismissRequest,
        mainActionLayout = ButtonLayout.TextOnly(stringResource(id = R.string.collections_limiter_member_cta)),
        title = stringResource(id = R.string.collections_limiter_member_title),
        description = { Text(text = stringResource(id = R.string.collections_limiter_member_description)) },
        isDestructive = false
    )
}

@Composable
private fun ContentLine2.getText(): String = when (this) {
    is ContentLine2.Text -> value
    is ContentLine2.SecureNoteSecured -> stringResource(id = R.string.secure_note_is_locked)
}

@Composable
private fun SectionType.getText(): String = when (this) {
    SectionType.SECTION_LOGIN -> stringResource(id = R.string.collection_detail_section_title_logins)
    SectionType.SECTION_SECURE_NOTE -> stringResource(id = R.string.collection_detail_section_title_secure_notes)
}

@Preview
@Composable
private fun PreviewHeader() {
    DashlanePreview {
        Header(title = SectionType.SECTION_LOGIN.getText())
    }
}

@Preview
@Composable
private fun PreviewContainer() {
    DashlanePreview {
        Column {
            Header(title = SectionType.SECTION_LOGIN.getText())
            VaultItemView(
                item = SummaryForUi(
                    id = "id",
                    type = SyncObjectXmlName.AUTHENTIFIANT,
                    firstLine = "first line",
                    secondLine = ContentLine2.Text("second line"),
                    thumbnail = ThumbnailData.UrlThumbnail(null),
                    sharingPermission = null,
                    spaceData = null
                ),
                shared = false,
                goToItem = { _, _ -> },
                removeItem = { _, _ -> }
            )
            BottomBackground()
        }
    }
}

@Preview
@Composable
private fun PreviewContainerSecureNote() {
    DashlanePreview {
        Column {
            Header(title = SectionType.SECTION_SECURE_NOTE.getText())
            VaultItemView(
                item = SummaryForUi(
                    id = "id",
                    type = SyncObjectXmlName.SECURE_NOTE,
                    firstLine = "first line",
                    secondLine = ContentLine2.Text("second line"),
                    thumbnail = ThumbnailData.SecureNoteThumbnail(secureNoteType = SyncObject.SecureNoteType.BLUE),
                    sharingPermission = null,
                    spaceData = null
                ),
                shared = false,
                goToItem = { _, _ -> },
                removeItem = { _, _ -> }
            )
            BottomBackground()
        }
    }
}