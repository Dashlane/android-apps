package com.dashlane.collections.list

import android.os.Bundle
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dashlane.R
import com.dashlane.collections.CollectionLoading
import com.dashlane.collections.DeleteConfirmationDialog
import com.dashlane.collections.SearchableTopAppBarTitle
import com.dashlane.collections.SpaceData
import com.dashlane.collections.details.DialogBusinessMemberLimit
import com.dashlane.design.component.ButtonLarge
import com.dashlane.design.component.ButtonLayout
import com.dashlane.design.component.Icon
import com.dashlane.design.component.Text
import com.dashlane.design.iconography.IconTokens
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.color.Intensity
import com.dashlane.design.theme.color.Mood
import com.dashlane.design.theme.color.TextColor
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.teamspaces.model.SpaceColor
import com.dashlane.ui.activities.fragments.AbstractContentFragment
import com.dashlane.ui.widgets.compose.OutlinedTeamspaceIcon
import com.dashlane.util.hideSoftKeyboard
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
@Suppress("LargeClass")
class CollectionsListFragment : AbstractContentFragment() {

    private val fabSize = 56.dp

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
                    CollectionsListScreen()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        (actionBarView.parent as? ViewGroup)?.removeView(actionBarView)
    }

    @Composable
    private fun CollectionsListScreen(viewModel: CollectionsListViewModel = viewModel()) {
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        val showSearch = remember { mutableStateOf(false) }
        val searchQuery = remember { mutableStateOf("") }
        val isSearching = remember { mutableStateOf(false) }
        var displayBusinessMemberLimitDialog by rememberSaveable { mutableStateOf(false) }
        val menuHost: MenuHost = requireActivity()
        val menuProvider = remember {
            object : MenuProvider {
                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    menuInflater.inflate(R.menu.collection_list_menu, menu)
                    val showMenuItems = uiState is ViewState.List
                    menu.findItem(R.id.menu_search).isVisible = showMenuItems && !showSearch.value
                    menu.findItem(R.id.menu_close).isVisible = showMenuItems && showSearch.value
                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                    return when (menuItem.itemId) {
                        R.id.menu_search -> {
                            showSearch.value = true
                            menuHost.invalidateMenu()
                            requireActivity().invalidateOptionsMenu()
                            true
                        }
                        R.id.menu_close -> {
                            showSearch.value = false
                            isSearching.value = false
                            searchQuery.value = ""
                            requireActivity().hideSoftKeyboard()
                            menuHost.invalidateMenu()
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
        SearchableActionBar(showSearch, searchQuery, isSearching)
        Scaffold(
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        navigator.goToCollectionAddFromCollectionsList()
                    },
                    modifier = Modifier.size(fabSize),
                    backgroundColor = DashlaneTheme.colors.containerExpressiveBrandCatchyIdle,
                ) {
                    Icon(
                        modifier = Modifier.size(16.dp),
                        token = IconTokens.actionAddOutlined,
                        contentDescription = null,
                        tint = DashlaneTheme.colors.textInverseCatchy
                    )
                }
            }
        ) {
            Box(modifier = Modifier.padding(it)) {
                when (uiState) {
                    is ViewState.List -> {
                        CollectionsList(
                            uiState.viewData,
                            searchQuery.value
                        ) { value -> displayBusinessMemberLimitDialog = value }
                        menuHost.invalidateMenu()
                        if (displayBusinessMemberLimitDialog) {
                            DialogBusinessMemberLimit {
                                displayBusinessMemberLimitDialog = false
                            }
                        }
                    }

                    is ViewState.Empty -> {
                        EmptyState()
                        menuHost.invalidateMenu()
                    }

                    is ViewState.Loading -> {
                        CollectionLoading()
                        menuHost.invalidateMenu()
                    }
                }
            }
        }
    }

    @Composable
    private fun SearchableActionBar(
        showSearch: MutableState<Boolean>,
        searchQuery: MutableState<String>,
        isSearching: MutableState<Boolean>
    ) {
        LaunchedEffect(key1 = showSearch.value) {
            actionBarView.setContent {
                DashlaneTheme {
                    SearchableTopAppBarTitle(
                        showSearch = showSearch.value,
                        searchLabel = stringResource(R.string.collection_list_search_hint),
                        title = stringResource(R.string.collections_list_title),
                        searchQuery = searchQuery,
                        isSearching = isSearching
                    )
                }
            }
            if (actionBarView.parent == null) {
                (activity as AppCompatActivity).supportActionBar?.customView = actionBarView
            }
        }
    }

    @Composable
    private fun CollectionsList(
        viewData: ViewData,
        searchQuery: String,
        onDisplayBusinessMemberLimitDialogChange: (Boolean) -> Unit
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = 16.dp,
                bottom = 16.dp + fabSize
            ) 
        ) {
            val filteredCollections = if (searchQuery.isEmpty()) {
                viewData.collections
            } else {
                viewData.collections.filter { it.name.contains(searchQuery, ignoreCase = true) }
            }
            items(filteredCollections) { collection ->
                CollectionItem(collection, onDisplayBusinessMemberLimitDialogChange)
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
                text = stringResource(id = R.string.collections_list_empty_state_description),
                style = DashlaneTheme.typography.bodyStandardRegular,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            ButtonLarge(
                onClick = {
                    navigator.goToCollectionAddFromCollectionsList()
                },
                layout = ButtonLayout.IconLeading(
                    iconToken = IconTokens.actionAddOutlined,
                    text = stringResource(id = R.string.collections_list_empty_state_button)
                ),
                mood = Mood.Brand,
                intensity = Intensity.Catchy
            )
        }
    }

    @Composable
    private fun CollectionItem(
        item: CollectionViewData,
        onDisplayBusinessMemberLimitDialogChange: (Boolean) -> Unit,
        viewModel: CollectionsListViewModel = viewModel()
    ) {
        var expanded by remember { mutableStateOf(false) }
        CollectionItem(
            item = item,
            expandMenu = expanded,
            shareEnabled = item.shareEnabled,
            shareAllowed = item.shareAllowed,
            onExpandMenuChange = { expanded = it },
            onDeleteClicked = viewModel::deleteClicked,
            onDisplayBusinessMemberLimitDialogChange = onDisplayBusinessMemberLimitDialogChange
        )
    }

    @Suppress("LongMethod")
    @Composable
    private fun CollectionItem(
        item: CollectionViewData,
        expandMenu: Boolean,
        shareEnabled: Boolean,
        shareAllowed: Boolean,
        onExpandMenuChange: (Boolean) -> Unit,
        onDeleteClicked: (String) -> Unit,
        onDisplayBusinessMemberLimitDialogChange: (Boolean) -> Unit,
    ) {
        var displayDeleteDialog by rememberSaveable { mutableStateOf(false) }
        Row(
            modifier = Modifier
                .clickable {
                    navigator.goToCollectionDetailsFromCollectionsList(
                        collectionId = item.id,
                        businessSpace = item.spaceData?.businessSpace ?: false,
                        sharedCollection = item.shared,
                        shareAllowed = item.shareAllowed,
                        shareEnabled = item.shareEnabled
                    )
                }
                .padding(vertical = 8.dp)
                .padding(start = 12.dp)
        ) {
            Column(modifier = Modifier.weight(weight = 1f, fill = true)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        modifier = Modifier.weight(weight = 1f, fill = false),
                        text = item.name,
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
                            iconSize = 12.dp
                        )
                    }

                    if (item.shared) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            token = IconTokens.sharedOutlined,
                            modifier = Modifier.size(12.dp),
                            contentDescription = context?.getString(R.string.and_accessibility_collection_list_item_shared)
                        )
                    }
                }
                Text(
                    text = pluralStringResource(
                        id = R.plurals.collections_list_collection_item_count,
                        count = item.itemCount,
                        item.itemCount
                    ),
                    style = DashlaneTheme.typography.bodyReducedRegular,
                    color = DashlaneTheme.colors.textNeutralQuiet
                )
            }

            CollectionItemDropdownMenu(
                onExpandMenuChange,
                expandMenu,
                shareEnabled,
                shareAllowed,
                item,
                onDisplayBusinessMemberLimitDialogChange,
                displayDeleteDialog
            )
        }

        if (displayDeleteDialog) {
            DeleteConfirmationDialog(
                onDismiss = {
                    displayDeleteDialog = false
                },
                onConfirm = {
                    onDeleteClicked(item.id)
                    displayDeleteDialog = false
                }
            )
        }
    }

    @Composable
    private fun CollectionItemDropdownMenu(
        onExpandMenuChange: (Boolean) -> Unit,
        expandMenu: Boolean,
        shareEnabled: Boolean,
        shareAllowed: Boolean,
        item: CollectionViewData,
        onDisplayBusinessMemberLimitDialogChange: (Boolean) -> Unit,
        displayDeleteDialog: Boolean
    ) {
        var displayDeleteDialog1 = displayDeleteDialog
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
                    if (shareEnabled) {
                        TextMenuItem(
                            stringResource(id = R.string.collections_list_item_menu_share),
                            enabled = shareAllowed
                        ) {
                            onExpandMenuChange(false)
                            if (!shareAllowed) return@TextMenuItem
                            if (item.shareLimitedByTeam) {
                                onDisplayBusinessMemberLimitDialogChange(true)
                            } else {
                                navigator.goToCollectionShareFromCollectionList(item.id)
                            }
                        }
                        if (item.shared) {
                            TextMenuItem(stringResource(id = R.string.collections_list_item_menu_shared_access)) {
                                onExpandMenuChange(false)
                                navigator.goToCollectionSharedAccessFromCollectionsList(item.id)
                            }
                        }
                    }
                    TextMenuItem(
                        stringResource(id = R.string.collections_list_item_menu_edit),
                        enabled = !item.shared
                    ) {
                        onExpandMenuChange(false)
                        navigator.goToCollectionEditFromCollectionsList(item.id)
                    }
                    TextMenuItem(
                        stringResource(id = R.string.collections_list_item_menu_delete),
                        enabled = !item.shared
                    ) {
                        onExpandMenuChange(false)
                        displayDeleteDialog1 = true
                    }
                }
            }
        }
    }

    @Composable
    private fun TextMenuItem(label: String, enabled: Boolean = true, onClick: () -> Unit) {
        val textColor =
            if (enabled) TextColor.Unspecified else DashlaneTheme.colors.textOddityDisabled
        DropdownMenuItem(onClick = onClick, enabled = enabled) {
            Text(text = label, color = textColor)
        }
    }

    @Preview
    @Composable
    fun PreviewBusinessCollectionItem() {
        DashlanePreview {
            CollectionItem(
                CollectionViewData(
                    id = "",
                    name = "Entertainment",
                    itemCount = 5,
                    SpaceData(
                        spaceLetter = 'E',
                        spaceColor = SpaceColor.TeamColor(Color.Magenta.toArgb()),
                        spaceContentDescriptionResId = R.string.and_accessibility_collection_list_item_business_teamspace,
                        businessSpace = true
                    ),
                    shared = true,
                    shareEnabled = true,
                    shareAllowed = true,
                    shareLimitedByTeam = true
                ),
                expandMenu = false,
                shareAllowed = true,
                shareEnabled = true,
                onExpandMenuChange = {},
                onDeleteClicked = {},
                onDisplayBusinessMemberLimitDialogChange = {}
            )
        }
    }

    @Preview
    @Composable
    fun PreviewBusinessCollectionItemWithLongName() {
        DashlanePreview {
            CollectionItem(
                CollectionViewData(
                    id = "",
                    name = "Very long collection name so that it cannot fit in the screen on one line",
                    itemCount = 5,
                    SpaceData(
                        spaceLetter = 'E',
                        spaceColor = SpaceColor.TeamColor(Color.Magenta.toArgb()),
                        spaceContentDescriptionResId = R.string.and_accessibility_collection_list_item_business_teamspace,
                        businessSpace = true
                    ),
                    shared = true,
                    shareEnabled = true,
                    shareAllowed = true,
                    shareLimitedByTeam = false
                ),
                expandMenu = false,
                shareAllowed = true,
                shareEnabled = true,
                onExpandMenuChange = {},
                onDeleteClicked = {},
                onDisplayBusinessMemberLimitDialogChange = {}
            )
        }
    }

    @Preview
    @Composable
    fun PreviewNoSpaceCollectionItem() {
        DashlanePreview {
            CollectionItem(
                item = CollectionViewData(
                    id = "",
                    name = "Entertainment",
                    itemCount = 1,
                    spaceData = null,
                    shared = true,
                    shareEnabled = true,
                    shareAllowed = false,
                    shareLimitedByTeam = false
                ),
                expandMenu = false,
                shareAllowed = true,
                shareEnabled = true,
                onExpandMenuChange = {},
                onDeleteClicked = {},
                onDisplayBusinessMemberLimitDialogChange = {}
            )
        }
    }

    @Preview
    @Composable
    fun PreviewEmptyState() {
        DashlanePreview {
            EmptyState()
        }
    }
}