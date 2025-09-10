package com.dashlane.item.collection

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dashlane.R
import com.dashlane.design.component.ButtonLayout
import com.dashlane.design.component.ButtonMedium
import com.dashlane.design.component.Dialog
import com.dashlane.design.component.DialogActionType
import com.dashlane.design.component.Icon
import com.dashlane.design.component.Text
import com.dashlane.design.component.TextField
import com.dashlane.design.component.cardBackground
import com.dashlane.design.component.tooling.TextFieldActions
import com.dashlane.design.iconography.IconTokens
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.ui.widgets.view.CollectionTag

@Composable
fun CollectionSelectorScreen(viewModel: CollectionSelectorViewModel) {
    val uiState by viewModel.uiState.viewState.collectAsState()
    CollectionSelector(
        userPrompt = uiState.userPrompt,
        collections = uiState.collectionsFilteredForDisplay,
        onUpdateUserPrompt = viewModel::updateUserPrompt,
        onClickCollection = viewModel::onClickCollection,
    )
    uiState.confirmAddToSharedCollectionDialogData?.let { confirmAddToSharedCollectionDialogData ->
        ConfirmDialog(
            collection = confirmAddToSharedCollectionDialogData.collection,
            isLimited = confirmAddToSharedCollectionDialogData.isLimited,
            onCancel = viewModel::cancelAddToSharedCollection,
            onConfirm = viewModel::confirmAddToSharedCollection,
        )
    }
    if (uiState.pendingChangeDialogData != null) {
        PendingChangeDialog(
            onDismissRequest = viewModel::cancelPendingChangeDialog,
            onSave = viewModel::save,
            onDiscard = viewModel::discardPendingChangeDialog,
        )
    }
}

@Composable
fun PendingChangeDialog(
    onDismissRequest: () -> Unit,
    onSave: () -> Unit,
    onDiscard: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        title = stringResource(id = R.string.collection_selector_dialog_pending_changes_title),
        description = {
            Text(
                text = stringResource(id = R.string.collection_selector_dialog_pending_changes_description),
            )
        },
        mainActionLayout = ButtonLayout.TextOnly(
            stringResource(id = R.string.collection_selector_dialog_pending_changes_save_button),
        ),
        mainActionClick = onSave,
        additionalActionLayout = ButtonLayout.TextOnly(
            stringResource(id = R.string.collection_selector_dialog_pending_changes_discard_button),
        ),
        additionalActionClick = onDiscard,
    )
}

@Composable
fun ConfirmDialog(
    collection: CollectionSelectorState.Collection,
    isLimited: Boolean,
    onCancel: () -> Unit,
    onConfirm: (CollectionSelectorState.Collection) -> Unit,
) {
    if (isLimited) {
        Dialog(
            onDismissRequest = onCancel,
            title = stringResource(id = R.string.collection_detail_dialog_limited_title),
            description = {
                Text(
                    text = stringResource(id = R.string.collection_detail_dialog_limited_description),
                )
            },
            mainActionLayout = ButtonLayout.TextOnly(
                stringResource(id = R.string.collection_detail_dialog_confirm_negative_button),
            ),
            mainActionClick = onCancel,
            mainActionType = DialogActionType.SECONDARY,
        )
    } else {
        Dialog(
            onDismissRequest = onCancel,
            title = stringResource(id = R.string.collection_detail_dialog_confirm_title),
            description = {
                Text(
                    text = stringResource(
                        id = R.string.collection_detail_dialog_confirm_text,
                        collection.name,
                    ),
                )
            },
            mainActionLayout = ButtonLayout.TextOnly(
                stringResource(id = R.string.collection_detail_dialog_confirm_positive_button),
            ),
            mainActionClick = { onConfirm(collection) },
            additionalActionLayout = ButtonLayout.TextOnly(
                stringResource(id = R.string.collection_detail_dialog_confirm_negative_button),
            ),
            additionalActionClick = onCancel,
        )
    }
}

@Composable
fun CollectionSelector(
    userPrompt: String,
    collections: List<CollectionSelectorState.Collection>,
    onUpdateUserPrompt: (String) -> Unit,
    onClickCollection: (CollectionSelectorState.Collection) -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(modifier = Modifier.widthIn(max = 480.dp)) {
            val focusRequester = remember { FocusRequester() }
            TextField(
                value = userPrompt,
                onValueChange = onUpdateUserPrompt,
                label = stringResource(id = R.string.collection_search_field_label),
                placeholder = stringResource(id = com.dashlane.ui.R.string.collection_search_field_placeholder),
                actions = TextFieldActions.ClearField(
                    contentDescription = stringResource(id = R.string.and_accessibility_action_text_clear),
                ) {
                    onUpdateUserPrompt("")
                    true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp),
            )
            LaunchedEffect(Unit) {
                focusRequester.requestFocus()
            }
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 16.dp),
            ) {
                if (collections.isNotEmpty()) {
                    item {
                        CollectionListHeader()
                    }

                    items(collections, key = { it.id ?: it.name }) { collection ->
                        CollectionRow(collection, onClickCollection)
                    }

                    item {
                        CollectionListFooter()
                    }
                }
            }
        }
    }
}

@Composable
private fun CollectionListHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, start = 16.dp, end = 16.dp)
            .cardBackground(isBottom = false)
            .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 8.dp),
    ) {
        Text(
            modifier = Modifier.semantics {
                heading()
            },
            text = stringResource(R.string.collection_selector_collection_list_header),
            style = DashlaneTheme.typography.titleBlockMedium,
        )
    }
}

@Composable
private fun CollectionListFooter() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
            .cardBackground(isTop = false)
            .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 8.dp),
    )
}

@Composable
private fun CollectionRow(
    collection: CollectionSelectorState.Collection,
    onClickCollection: (CollectionSelectorState.Collection) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .semantics {
                selected = collection.selected
            }
            .clickable(
                onClickLabel = when {
                    collection.forCreation -> stringResource(R.string.and_accessibility_collection_list_item_create)
                    collection.selected -> stringResource(R.string.and_accessibility_collection_list_item_deselection)
                    else -> stringResource(R.string.and_accessibility_collection_list_item_selection)
                },
            ) {
                onClickCollection(collection)
            }
            .cardBackground(isTop = false, isBottom = false)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        CollectionTag(
            modifier = Modifier
                .padding(top = 8.dp, bottom = 8.dp, end = 16.dp)
                .weight(1f, fill = false),
            label = collection.name,
            shared = collection.shared,
        )
        if (collection.forCreation) {
            ButtonMedium(
                onClick = { onClickCollection(collection) },
                layout = ButtonLayout.TextOnly(
                    text = stringResource(R.string.collection_create_button),
                ),
            )
        } else if (collection.selected) {
            Icon(
                token = IconTokens.checkmarkOutlined,
                contentDescription = null,
                tint = DashlaneTheme.colors.textBrandStandard,
            )
        }
    }
}

@Composable
@Preview
private fun CollectionSelectorPreview() {
    DashlanePreview {
        CollectionSelector(
            collections = listOf(
                CollectionSelectorState.Collection(
                    name = "Stor",
                    shared = false,
                    selected = false,
                    initiallySelected = false,
                    forCreation = true,
                ),
                CollectionSelectorState.Collection(
                    name = "Storage",
                    shared = true,
                    selected = false,
                    initiallySelected = false,
                ),
                CollectionSelectorState.Collection(
                    name = "Storytelling",
                    shared = false,
                    selected = true,
                    initiallySelected = false,
                ),
                CollectionSelectorState.Collection(
                    name = "Stores",
                    shared = false,
                    selected = false,
                    initiallySelected = true,
                ),
            ),
            userPrompt = "Stor",
            onUpdateUserPrompt = { },
            onClickCollection = { },
        )
    }
}