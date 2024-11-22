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
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.FloatingActionButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dashlane.R
import com.dashlane.collections.CollectionLoading
import com.dashlane.collections.DeleteConfirmationDialog
import com.dashlane.collections.RevokeToDeleteDialog
import com.dashlane.collections.SearchableTopAppBarTitle
import com.dashlane.collections.SpaceData
import com.dashlane.collections.details.DialogBusinessMemberLimit
import com.dashlane.design.component.ButtonLarge
import com.dashlane.design.component.ButtonLayout
import com.dashlane.design.component.ButtonMedium
import com.dashlane.design.component.DropdownItem
import com.dashlane.design.component.DropdownMenu
import com.dashlane.design.component.Icon
import com.dashlane.design.component.ListItem
import com.dashlane.design.component.ListItemActions
import com.dashlane.design.component.Text
import com.dashlane.design.component.cardBackground
import com.dashlane.design.iconography.IconTokens
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.color.Intensity
import com.dashlane.design.theme.color.Mood
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.design.theme.typography.withSearchHighlight
import com.dashlane.teamspaces.model.SpaceColor
import com.dashlane.ui.activities.fragments.AbstractContentFragment
import com.dashlane.ui.widgets.compose.OutlinedTeamspaceIcon
import com.dashlane.util.SnackbarUtils
import com.dashlane.util.getBaseActivity
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

    @Suppress("LongMethod")
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
                    is ViewState.List,
                    is ViewState.RevokeAccessPrompt,
                    is ViewState.SharedCollectionDeleteError -> {
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
                        val state = uiState
                        if (state is ViewState.RevokeAccessPrompt) {
                            RevokeToDeleteDialog(
                                onDismiss = {
                                    viewModel.dismissDialogClicked()
                                },
                                onConfirm = {
                                    navigator.goToCollectionSharedAccessFromCollectionsList(collectionId = state.collectionId)
                                }
                            )
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
        val filteredCollections = remember(searchQuery, viewData.collections) {
            if (searchQuery.isEmpty()) {
                viewData.collections
            } else {
                viewData.collections.filter { it.name.contains(searchQuery, ignoreCase = true) }
            }
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = 16.dp,
                bottom = 16.dp + fabSize
            ) 
        ) {
            itemsIndexed(
                items = filteredCollections,
                key = { _, item -> item.id }
            ) { index, collection ->
                CollectionItem(
                    item = collection,
                    onDisplayBusinessMemberLimitDialogChange = onDisplayBusinessMemberLimitDialogChange,
                    isFirst = index == 0,
                    isLast = index == filteredCollections.size - 1,
                    searchQuery = searchQuery,
                )
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
        isFirst: Boolean,
        isLast: Boolean,
        searchQuery: String,
        viewModel: CollectionsListViewModel = viewModel()
    ) {
        var expanded by remember { mutableStateOf(false) }
        CollectionItem(
            item = item,
            isFirst = isFirst,
            isLast = isLast,
            searchQuery = searchQuery,
            expandMenu = expanded,
            onExpandMenuChange = { expanded = it },
            onDeleteClicked = viewModel::deleteClicked,
            onDisplayBusinessMemberLimitDialogChange = onDisplayBusinessMemberLimitDialogChange
        )
    }

    @Suppress("kotlin:S1066", "LongMethod")
    @Composable
    private fun CollectionItem(
        item: CollectionViewData,
        isFirst: Boolean,
        isLast: Boolean,
        searchQuery: String,
        expandMenu: Boolean,
        onExpandMenuChange: (Boolean) -> Unit,
        onDeleteClicked: (String, Boolean) -> Unit,
        onDisplayBusinessMemberLimitDialogChange: (Boolean) -> Unit,
    ) {
        var displayDeleteDialog by rememberSaveable { mutableStateOf(false) }
        val showSpaceIndicator = remember(item.spaceData) {
            item.spaceData != null
        }
        val showSharedIcon = remember(item.shared) {
            item.shared
        }
        ListItem(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .cardBackground(isTop = isFirst, isBottom = isLast)
                .padding(
                    top = if (isFirst) 8.dp else 0.dp,
                    bottom = if (isLast) 8.dp else 0.dp,
                    start = 8.dp,
                    end = 8.dp,
                ),
            thumbnailType = null,
            title = item.name.withSearchHighlight(match = searchQuery, ignoreCase = true),
            titleExtraContent = {
                if (showSpaceIndicator) {
                    
                    if (item.spaceData != null) {
                        OutlinedTeamspaceIcon(
                            letter = item.spaceData.spaceLetter,
                            color = when (val color = item.spaceData.spaceColor) {
                                is SpaceColor.FixColor -> colorResource(color.colorRes).toArgb()
                                is SpaceColor.TeamColor -> color.color
                            },
                            iconSize = 16.dp,
                            teamspaceDescription = stringResource(id = item.spaceData.spaceContentDescriptionResId),
                        )
                    }
                }
                if (showSharedIcon) {
                    Icon(
                        token = IconTokens.sharedOutlined,
                        modifier = Modifier.size(12.dp),
                        contentDescription = context?.getString(R.string.and_accessibility_collection_list_item_shared)
                    )
                }
            },
            description = AnnotatedString(
                pluralStringResource(
                    id = R.plurals.collections_list_collection_item_count,
                    count = item.itemCount,
                    item.itemCount
                )
            ),
            actions = buildCollectionActionsMenu(
                onExpandMenuChange = onExpandMenuChange,
                expandMenu = expandMenu,
                item = item,
                onDisplayBusinessMemberLimitDialogChange = onDisplayBusinessMemberLimitDialogChange,
                onDisplayDeleteDialogChange = { displayDeleteDialog = it }
            )
        ) {
            navigator.goToCollectionDetailsFromCollectionsList(
                collectionId = item.id,
                businessSpace = item.spaceData?.businessSpace ?: false,
                sharedCollection = item.shared,
                shareAllowed = item.shareAllowed
            )
        }

        if (displayDeleteDialog) {
            DeleteConfirmationDialog(
                onDismiss = {
                    displayDeleteDialog = false
                },
                onConfirm = {
                    onDeleteClicked(item.id, item.shared)
                    displayDeleteDialog = false
                }
            )
        }
    }

    @Composable
    private fun buildCollectionActionsMenu(
        onExpandMenuChange: (Boolean) -> Unit,
        expandMenu: Boolean,
        item: CollectionViewData,
        onDisplayBusinessMemberLimitDialogChange: (Boolean) -> Unit,
        onDisplayDeleteDialogChange: (Boolean) -> Unit
    ): ListItemActions {
        val shareLabel = stringResource(id = R.string.collections_list_item_menu_share)
        val sharedAccessLabel = stringResource(id = R.string.collections_list_item_menu_shared_access)
        val editLabel = stringResource(id = R.string.collections_list_item_menu_edit)
        val deleteLabel = stringResource(id = R.string.collections_list_item_menu_delete)
        return object : ListItemActions {
            @Composable
            override fun Content() {
                DropdownMenu(
                    anchor = {
                        ButtonMedium(
                            modifier = Modifier.clearAndSetSemantics { },
                            onClick = { onExpandMenuChange(true) },
                            intensity = Intensity.Supershy,
                            layout = ButtonLayout.IconOnly(
                                iconToken = IconTokens.actionMoreOutlined,
                                
                                contentDescription = stringResource(R.string.abc_action_menu_overflow_description),
                            ),
                        )
                    },
                    expanded = expandMenu,
                    onDismissRequest = { onExpandMenuChange(false) },
                ) {
                    if (item.shareEnabled) {
                        DropdownItem(
                            icon = null,
                            text = shareLabel,
                            enabled = item.shareAllowed,
                        ) {
                            onExpandMenuChange(false)
                            onShareClicked(item, onDisplayBusinessMemberLimitDialogChange)
                        }
                    }
                    if (item.shareEnabled && item.shared) {
                        DropdownItem(
                            icon = null,
                            text = sharedAccessLabel,
                        ) {
                            onExpandMenuChange(false)
                            onSharedAccessClicked(item)
                        }
                    }
                    DropdownItem(
                        icon = null,
                        text = editLabel,
                        enabled = !item.shared || item.editAllowed,
                    ) {
                        onExpandMenuChange(false)
                        onEditClicked(item)
                    }
                    DropdownItem(
                        icon = null,
                        text = deleteLabel,
                        enabled = item.deleteAllowed,
                    ) {
                        onExpandMenuChange(false)
                        onDisplayDeleteDialogChange(true)
                    }
                }
            }

            override fun getCustomAccessibilityActions(): List<CustomAccessibilityAction> {
                val customActions = mutableListOf<CustomAccessibilityAction>()
                if (item.shareEnabled && item.shareAllowed) {
                    val shareAction = CustomAccessibilityAction(
                        label = shareLabel,
                    ) {
                        onShareClicked(item, onDisplayBusinessMemberLimitDialogChange)
                        true
                    }
                    customActions.add(shareAction)
                }
                if (item.shareEnabled && item.shared) {
                    val sharedAccessAction = CustomAccessibilityAction(
                        label = sharedAccessLabel
                    ) {
                        onSharedAccessClicked(item)
                        true
                    }
                    customActions.add(sharedAccessAction)
                }
                if (!item.shared || item.editAllowed) {
                    val editAction = CustomAccessibilityAction(
                        label = editLabel,
                    ) {
                        onEditClicked(item)
                        true
                    }
                    customActions.add(editAction)
                }
                if (item.deleteAllowed) {
                    val deleteAction = CustomAccessibilityAction(
                        label = deleteLabel,
                    ) {
                        onDisplayDeleteDialogChange(true)
                        true
                    }
                    customActions.add(deleteAction)
                }
                return customActions
            }
        }
    }

    private fun onShareClicked(
        item: CollectionViewData,
        onDisplayBusinessMemberLimitDialogChange: (Boolean) -> Unit
    ) {
        if (!item.shareAllowed) return
        if (item.shareLimitedByTeam) {
            onDisplayBusinessMemberLimitDialogChange(true)
        } else {
            navigator.goToCollectionShareFromCollectionList(item.id)
        }
    }

    private fun onSharedAccessClicked(item: CollectionViewData) {
        navigator.goToCollectionSharedAccessFromCollectionsList(item.id)
    }

    private fun onEditClicked(item: CollectionViewData) {
        navigator.goToCollectionEditFromCollectionsList(item.id, item.shared)
    }

    @Preview
    @Composable
    private fun PreviewBusinessCollectionItem() {
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
                    shareLimitedByTeam = true,
                    editAllowed = true,
                    deleteAllowed = true
                ),
                isFirst = false,
                isLast = false,
                searchQuery = "Ent",
                expandMenu = false,
                onExpandMenuChange = {},
                onDeleteClicked = { _, _ -> },
                onDisplayBusinessMemberLimitDialogChange = {}
            )
        }
    }

    @Preview
    @Composable
    private fun PreviewBusinessCollectionItemWithLongName() {
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
                    shareLimitedByTeam = false,
                    editAllowed = true,
                    deleteAllowed = true
                ),
                isFirst = false,
                isLast = false,
                searchQuery = "name",
                expandMenu = false,
                onExpandMenuChange = {},
                onDeleteClicked = { _, _ -> },
                onDisplayBusinessMemberLimitDialogChange = {}
            )
        }
    }

    @Preview
    @Composable
    private fun PreviewNoSpaceCollectionItem() {
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
                    shareLimitedByTeam = false,
                    editAllowed = false,
                    deleteAllowed = false
                ),
                isFirst = false,
                isLast = false,
                searchQuery = "Ent",
                expandMenu = false,
                onExpandMenuChange = {},
                onDeleteClicked = { _, _ -> },
                onDisplayBusinessMemberLimitDialogChange = {}
            )
        }
    }

    @Preview
    @Composable
    private fun PreviewEmptyState() {
        DashlanePreview {
            EmptyState()
        }
    }
}