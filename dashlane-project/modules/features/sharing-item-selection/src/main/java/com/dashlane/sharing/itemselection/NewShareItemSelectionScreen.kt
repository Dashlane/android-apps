package com.dashlane.sharing.itemselection

import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dashlane.design.component.ButtonLarge
import com.dashlane.design.component.ButtonLayout
import com.dashlane.design.component.SearchField
import com.dashlane.design.component.Text
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.color.Intensity
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.sharing.itemselection.NewShareItemSelectionEvent.ViewStarted
import com.dashlane.vault.item.VaultListItem
import com.dashlane.vault.item.VaultListItemState

@Composable
fun NewShareItemSelectionScreen(
    viewModel: NewShareItemSelectionViewModel = viewModel(),
    onLockRequest: () -> Unit,
    goToNewShare: (selectedPasswords: Array<String>, selectedNotes: Array<String>, selectedSecrets: Array<String>) -> Unit,
) {
    val uiState by viewModel.stateFlow.viewState.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) {
        viewModel.sendEvent(ViewStarted)
        viewModel.stateFlow.sideEffect.collect {
            when (it) {
                NewShareItemSelectionState.SideEffect.MPLock -> {
                    onLockRequest()
                }
                is NewShareItemSelectionState.SideEffect.GoToNewShare -> {
                    goToNewShare(
                        it.selectedPasswords.toTypedArray(),
                        it.selectedNotes.toTypedArray(),
                        it.selectedSecrets.toTypedArray(),
                    )
                }
            }
        }
    }

    var query by rememberSaveable { mutableStateOf("") }
    NewShareItemSelectionScreenContent(
        query = query,
        displayList = uiState.data.displayList ?: emptyList(),
        showFooter = uiState.data.showFooter,
        selectedItemCount = uiState.data.selectedItemCounts,
        onQueryChange = {
            query = it
            viewModel.sendEvent(NewShareItemSelectionEvent.QueryChanged(it))
        },
        onItemClicked = { item ->
            viewModel.sendEvent(NewShareItemSelectionEvent.ItemClicked(item))
        },
        onClickNewShare = {
            viewModel.sendEvent(NewShareItemSelectionEvent.NewShareClicked)
        },
    )
}

@Composable
@VisibleForTesting
fun NewShareItemSelectionScreenContent(
    query: String,
    displayList: List<ListItemState>,
    showFooter: Boolean,
    selectedItemCount: Int,
    onQueryChange: (String) -> Unit,
    onItemClicked: (VaultListItemState) -> Unit,
    onClickNewShare: () -> Unit,
) {
    Column {
        SearchField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, start = 16.dp, end = 16.dp),
            value = query,
            onValueChange = onQueryChange,
            placeholder = stringResource(R.string.share_item_selection_search_bar_label),
        )
        ShareItemSelectionList(
            modifier = Modifier.weight(weight = 1f, fill = true),
            list = displayList,
            onItemClicked = onItemClicked,
        )

        if (showFooter) {
            FooterShareItemSelection(selectedItemCount, onClickNewShare)
        }
    }
}

@Composable
private fun ShareItemSelectionList(
    modifier: Modifier = Modifier,
    list: List<ListItemState>,
    onItemClicked: (VaultListItemState) -> Unit,
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
    ) {
        items(items = list, key = { it.key }) { item ->
            when (item) {
                is ListItemState.HeaderItemState -> {
                    Text(
                        modifier = Modifier.padding(
                            top = 24.dp,
                            bottom = 8.dp,
                            start = 16.dp,
                            end = 16.dp,
                        ),
                        text = item.title,
                        style = DashlaneTheme.typography.titleSupportingSmall,
                        color = DashlaneTheme.colors.textNeutralQuiet,
                        enforceAllCaps = true,
                    )
                }
                is ListItemState.VaultItemState -> {
                    VaultListItem(
                        item = item.vaultListItemState,
                        onClick = {
                            onItemClicked(item.vaultListItemState)
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun FooterShareItemSelection(checkedItemCount: Int, onClickNewShare: () -> Unit) {
    Row(
        modifier = Modifier
            .background(color = DashlaneTheme.colors.containerAgnosticNeutralStandard)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            modifier = Modifier.weight(weight = 1f, fill = true),
            text = pluralStringResource(
                R.plurals.share_item_selection_items_selected,
                checkedItemCount,
                checkedItemCount,
            ),
            color = DashlaneTheme.colors.textNeutralQuiet,
            style = DashlaneTheme.typography.bodyStandardRegular,
        )
        ButtonLarge(
            modifier = Modifier.padding(start = 8.dp),
            onClick = onClickNewShare,
            layout = ButtonLayout.TextOnly(
                text = stringResource(R.string.share_item_selection_confirm_item_selection_button),
            ),
            intensity = Intensity.Catchy,
        )
    }
}

@Preview
@Composable
private fun NewShareItemSelectionScreenContentPreview() {
    DashlanePreview {
        NewShareItemSelectionScreenContent(
            query = "",
            displayList = listOf(
                ListItemState.HeaderItemState("A"),
                ListItemState.VaultItemState(
                    key = "item1",
                    vaultListItemState = VaultListItemState(
                        id = "item1",
                        title = "Amazon",
                        description = "Amazon account",
                        hasError = false,
                        isChecked = false,
                        isLockIconVisible = false,
                        thumbnail = VaultListItemState.ThumbnailState.VaultThumbnail.DomainIcon("https://www.amazon.com"),
                    ),
                ),
                ListItemState.VaultItemState(
                    key = "item2",
                    vaultListItemState = VaultListItemState(
                        id = "item2",
                        title = "Adobe",
                        description = "description",
                        hasError = false,
                        isChecked = true,
                        isLockIconVisible = false,
                        thumbnail = VaultListItemState.ThumbnailState.VaultThumbnail.DomainIcon("https://www.adobe.com"),
                    ),
                ),
                ListItemState.HeaderItemState("B"),
                ListItemState.VaultItemState(
                    key = "item3",
                    vaultListItemState = VaultListItemState(
                        id = "item3",
                        title = "B",
                        description = "Note is locked",
                        hasError = false,
                        isChecked = true,
                        isLockIconVisible = true,
                    ),
                ),
            ),
            selectedItemCount = 2,
            showFooter = true,
            onQueryChange = {},
            onItemClicked = {},
            onClickNewShare = {},
        )
    }
}