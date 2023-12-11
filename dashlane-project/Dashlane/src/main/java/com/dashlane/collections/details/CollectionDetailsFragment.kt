package com.dashlane.collections.details

import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
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
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dashlane.R
import com.dashlane.collections.CollectionLoading
import com.dashlane.collections.DeleteConfirmationDialog
import com.dashlane.design.component.Icon
import com.dashlane.design.component.Text
import com.dashlane.design.iconography.IconTokens
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.ui.activities.fragments.AbstractContentFragment
import com.dashlane.ui.widgets.compose.OutlinedTeamspaceIcon
import com.dashlane.ui.widgets.compose.urldomainicon.UrlDomainIcon
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CollectionDetailsFragment : AbstractContentFragment() {

    private lateinit var actionBarView: ComposeView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        actionBarView = ComposeView(requireContext())
        return ComposeView(requireContext()).apply {
            setContent {
                DashlaneTheme {
                    CollectionDetailsScreen()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        (actionBarView.parent as? ViewGroup)?.removeView(actionBarView)
    }

    @Composable
    private fun CollectionDetailsScreen(viewModel: CollectionDetailsViewModel = viewModel()) {
        val menuHost: MenuHost = requireActivity()
        val disabledTextColor = DashlaneTheme.colors.textOddityDisabled.value
        val menuProvider = remember {
            object : MenuProvider {
                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    menuInflater.inflate(R.menu.collection_detail_menu, menu)
                    menu.findItem(R.id.menu_share).isVisible = viewModel.navArgs.shareAllowed
                    menu.findItem(R.id.menu_shared_access).isVisible =
                        viewModel.navArgs.shareAllowed && viewModel.navArgs.sharedCollection
                    menu.findItem(R.id.menu_edit).updateStateAndTitleColor(
                        !viewModel.navArgs.sharedCollection,
                        disabledTextColor
                    )
                    menu.findItem(R.id.menu_delete).updateStateAndTitleColor(
                        !viewModel.navArgs.sharedCollection,
                        disabledTextColor
                    )
                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                    return when (menuItem.itemId) {
                        R.id.menu_delete -> {
                            viewModel.listeningChanges = true
                            viewModel.deleteClicked()
                            true
                        }
                        R.id.menu_edit -> {
                            viewModel.listeningChanges = true
                            navigator.goToCollectionEditFromCollectionDetail(viewModel.navArgs.collectionId)
                            true
                        }
                        R.id.menu_share -> {
                            
                            
                            viewModel.listeningChanges = !viewModel.navArgs.sharedCollection
                            navigator.goToCollectionShareFromCollectionDetail(viewModel.navArgs.collectionId)
                            true
                        }
                        else -> false
                    }
                }
            }
        }
        LaunchedEffect(key1 = menuProvider) {
            menuHost.addMenuProvider(menuProvider, viewLifecycleOwner)
        }

        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        actionBarView.setContent { ActionBar(uiState) }
        if (actionBarView.parent == null) {
            (activity as AppCompatActivity).supportActionBar?.customView = actionBarView
        }
        when (uiState) {
            is ViewState.List,
            is ViewState.DeletePrompt -> {
                ItemsList(
                    viewData = uiState.viewData,
                    showDeletePrompt = uiState is ViewState.DeletePrompt,
                    onDeleteDismissed = {
                        viewModel.dismissDeleteClicked()
                    },
                    onDeleteConfirmed = {
                        viewModel.confirmDeleteClicked()
                    }
                )
            }

            is ViewState.Empty -> {
                EmptyState()
            }

            is ViewState.Deleted -> {
                navigator.popBackStack()
            }

            is ViewState.Loading -> {
                CollectionLoading()
            }
        }
    }

    private fun MenuItem.updateStateAndTitleColor(
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
    private fun ActionBar(uiState: ViewState) {
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
                        color = spaceData.spaceColor,
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
                        contentDescription = context?.getString(R.string.and_accessibility_collection_list_item_shared)
                    )
                }
            }
        }
    }

    @Composable
    private fun EmptyState() {
        Column(
            modifier = Modifier.padding(32.dp),
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
    fun ItemsList(
        viewData: ViewData,
        showDeletePrompt: Boolean,
        onDeleteDismissed: () -> Unit,
        onDeleteConfirmed: () -> Unit
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            items(viewData.items) { item ->
                VaultItemView(item, viewData.shared)
            }
        }
        if (showDeletePrompt) {
            DeleteConfirmationDialog(
                onDismiss = {
                    onDeleteDismissed()
                },
                onConfirm = {
                    onDeleteConfirmed()
                }
            )
        }
    }

    @Composable
    private fun VaultItemView(
        item: SummaryForUi,
        shared: Boolean,
        viewModel: CollectionDetailsViewModel = viewModel()
    ) {
        var expanded by remember { mutableStateOf(false) }
        VaultItemView(
            item = item,
            shared = shared,
            expandMenu = expanded,
            onExpandMenuChange = { expanded = it },
            onRemoveItemClicked = viewModel::removeItem
        )
    }

    @Suppress("LongMethod")
    @Composable
    fun VaultItemView(
        item: SummaryForUi,
        shared: Boolean,
        expandMenu: Boolean,
        onExpandMenuChange: (Boolean) -> Unit,
        onRemoveItemClicked: (String) -> Unit
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { navigator.goToItem(item.id, item.type) }
                .padding(vertical = 8.dp)
                .padding(start = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            UrlDomainIcon(
                urlDomain = item.thumbnail,
                modifier = Modifier.size(62.dp, 34.dp),
                issuer = item.firstLine
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(weight = 1f, fill = true)) {
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
                            color = item.spaceData.spaceColor,
                            iconSize = 12.dp,
                            teamspaceDescription = stringResource(id = item.spaceData.spaceContentDescriptionResId)
                        )
                    }

                    if (shared) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            token = IconTokens.sharedOutlined,
                            modifier = Modifier.size(12.dp),
                            contentDescription = context?.getString(R.string.and_accessibility_collection_list_item_shared)
                        )
                    }
                }

                Text(
                    text = item.secondLine,
                    style = DashlaneTheme.typography.bodyReducedRegular,
                    color = DashlaneTheme.colors.textNeutralQuiet,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            if (shared) return@Row
            Box {
                IconButton(onClick = { onExpandMenuChange(true) }) {
                    Icon(token = IconTokens.actionMoreOutlined, contentDescription = null)
                }
                MaterialTheme(
                    
                    
                    colors = MaterialTheme.colors.copy(
                        surface = DashlaneTheme.colors.containerAgnosticNeutralSupershy
                    )
                ) {
                    DropdownMenu(
                        expanded = expandMenu,
                        onDismissRequest = {
                            onExpandMenuChange(false)
                        },
                        offset = DpOffset(x = 12.dp, y = 0.dp)
                    ) {
                        DropdownMenuItem(
                            onClick = {
                                onExpandMenuChange(false)
                                onRemoveItemClicked(item.id)
                            }
                        ) {
                            Text(text = stringResource(id = R.string.collection_details_item_menu_remove))
                        }
                    }
                }
            }
        }
    }
}