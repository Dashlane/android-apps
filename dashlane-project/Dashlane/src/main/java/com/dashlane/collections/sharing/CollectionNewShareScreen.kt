package com.dashlane.collections.sharing

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Divider
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dashlane.R
import com.dashlane.collections.CollectionLoading
import com.dashlane.collections.SearchableTopAppBarTitle
import com.dashlane.collections.sharing.NewCollectionShareViewState.Individual
import com.dashlane.collections.sharing.NewCollectionShareViewState.List
import com.dashlane.collections.sharing.NewCollectionShareViewState.Loading
import com.dashlane.collections.sharing.NewCollectionShareViewState.SharingFailed
import com.dashlane.collections.sharing.NewCollectionShareViewState.SharingSuccess
import com.dashlane.collections.sharing.NewCollectionShareViewState.UserGroup
import com.dashlane.collections.sharing.NewCollectionShareViewState.ViewData
import com.dashlane.design.component.ButtonLarge
import com.dashlane.design.component.ButtonLayout
import com.dashlane.design.component.ButtonMedium
import com.dashlane.design.component.Checkbox
import com.dashlane.design.component.Icon
import com.dashlane.design.component.InfoboxMedium
import com.dashlane.design.component.Text
import com.dashlane.design.component.tooling.setCheckboxSemantic
import com.dashlane.design.iconography.IconTokens
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.color.Intensity
import com.dashlane.design.theme.color.Mood
import com.dashlane.design.theme.color.Mood.Brand
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.ui.widgets.compose.contact.ContactIcon

@Composable
internal fun CollectionNewShareScreen(
    viewModel: CollectionsNewShareViewModel = viewModel(),
    userNavigationListener: UserNavigationListener
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val viewData = uiState.viewData
    val searchQuery = remember { mutableStateOf("") }
    val isSearching = remember { mutableStateOf(false) }
    Scaffold(
        topBar = {
            CollectionNewShareTopBar(
                uiState,
                viewData,
                searchQuery,
                isSearching,
                viewModel,
                userNavigationListener
            )
        }
    ) {
        Box(modifier = Modifier.padding(it)) {
            when (uiState) {
                is Loading -> CollectionLoading()
                is List ->
                    AvailableRecipientsList(
                        viewData,
                        viewData.showSharingButton,
                        searchQuery.value,
                        viewModel
                    )
                is SharingSuccess ->
                    userNavigationListener.onSharingSucceed(
                        collectionName = viewData.collectionName,
                        collectionId = viewData.sharedCollectionId!!,
                        
                        isBusiness = true
                    )
                is SharingFailed ->
                    userNavigationListener.onSharingFailed(viewData.collectionName)
            }
        }
    }
}

@Composable
private fun CollectionNewShareTopBar(
    uiState: NewCollectionShareViewState,
    viewData: ViewData,
    searchQuery: MutableState<String>,
    isSearching: MutableState<Boolean>,
    viewModel: CollectionsNewShareViewModel,
    userNavigationListener: UserNavigationListener
) {
    TopAppBar(
        title = {
            SearchableTopAppBarTitle(
                showSearch = uiState is List && viewData.showSearch,
                searchLabel = stringResource(R.string.collection_new_share_search_hint),
                title = stringResource(id = R.string.collection_new_share_title),
                searchQuery = searchQuery,
                isSearching = isSearching
            )
        },
        navigationIcon = {
            IconButton(
                onClick = {
                    if (viewData.showSearch) {
                        
                        isSearching.value = false
                        searchQuery.value = ""
                        viewModel.onToggleSearch()
                    } else {
                        userNavigationListener.onCloseClicked()
                    }
                }
            ) {
                Icon(
                    token = if (viewData.showSearch) {
                        IconTokens.arrowLeftOutlined
                    } else {
                        IconTokens.actionCloseOutlined
                    },
                    contentDescription = stringResource(id = R.string.and_accessibility_close),
                    modifier = Modifier.size(20.dp)
                )
            }
        },
        backgroundColor = DashlaneTheme.colors.containerAgnosticNeutralStandard,
        actions = {
            ButtonMedium(
                onClick = {
                    
                    viewModel.onToggleSearch()
                },
                layout = ButtonLayout.IconOnly(
                    IconTokens.actionSearchOutlined,
                    stringResource(R.string.collection_new_share_search_accessibility)
                ),
                mood = Mood.Neutral,
                intensity = Intensity.Supershy
            )
        }
    )
}

@Composable
private fun AvailableRecipientsList(
    viewData: ViewData,
    showSharingButton: Boolean,
    searchQuery: String,
    userInteractionListener: UserInteractionListener
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        if (searchQuery.isEmpty()) {
            InfoboxMedium(
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 8.dp),
                mood = Brand,
                title = stringResource(id = R.string.collection_new_share_infobox_title),
                description = stringResource(id = R.string.collection_new_share_infobox_description)
            )
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            val filteredUserGroups = if (searchQuery.isNotEmpty()) {
                viewData.userGroups.filter { it.name.contains(searchQuery, ignoreCase = true) }
            } else {
                viewData.userGroups
            }
            if (filteredUserGroups.isNotEmpty()) {
                item {
                    ListTitle(stringResource(R.string.collection_new_share_groups_section_title))
                }
                items(filteredUserGroups) { groups ->
                    UserGroupItem(groups, userInteractionListener::onGroupSelectionChange)
                }
            }
            val filteredIndividuals = if (searchQuery.isNotEmpty()) {
                viewData.individuals.filter {
                    it.username.contains(searchQuery, ignoreCase = true)
                }
            } else {
                viewData.individuals
            }
            if (filteredIndividuals.isNotEmpty()) {
                item { ListTitle(stringResource(R.string.collection_new_share_individuals_section_title)) }
                items(filteredIndividuals) { individuals ->
                    IndividualItem(
                        individuals,
                        userInteractionListener::onIndividualSelectionChange
                    )
                }
            }
            if (filteredIndividuals.isEmpty() && filteredUserGroups.isEmpty()) {
                
                item { NoResultsItem() }
            }
        }
        if (showSharingButton) ShareButton(userInteractionListener)
    }
}

@Composable
private fun ColumnScope.ShareButton(userInteractionListener: UserInteractionListener) {
    Divider(color = DashlaneTheme.colors.borderNeutralQuietIdle, thickness = 1.dp)
    ButtonLarge(
        onClick = { userInteractionListener.onShareClicked() },
        modifier = Modifier.Companion
            .align(Alignment.End)
            .padding(all = 16.dp),
        layout = ButtonLayout.TextOnly(stringResource(R.string.collection_new_share_primary_button)),
        mood = Brand,
        intensity = Intensity.Catchy
    )
}

@Composable
private fun NoResultsItem() {
    Text(
        text = stringResource(id = R.string.collection_new_share_empty),
        modifier = Modifier.padding(all = 16.dp),
        style = DashlaneTheme.typography.bodyStandardRegular,
        color = DashlaneTheme.colors.textWarningStandard,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
private fun UserGroupItem(group: UserGroup, onSelectionChange: (UserGroup) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .setCheckboxSemantic(
                value = group.selected,
                enabled = true,
            ) { onSelectionChange.invoke(group) }
            .padding(start = 12.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically

    ) {
        Checkbox(checked = group.selected)
        Spacer(modifier = Modifier.width(8.dp))
        GroupIcon()
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = group.name,
                style = DashlaneTheme.typography.bodyStandardRegular,
                color = DashlaneTheme.colors.textNeutralCatchy,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = pluralStringResource(
                    id = R.plurals.collections_new_share_group_members,
                    count = group.membersCount,
                    formatArgs = arrayOf(group.membersCount)
                ),
                style = DashlaneTheme.typography.bodyReducedRegular,
                color = DashlaneTheme.colors.textNeutralQuiet,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun GroupIcon() {
    Icon(
        modifier = Modifier
            .size(36.dp)
            .background(
                color = DashlaneTheme.colors.containerAgnosticNeutralStandard,
                shape = CircleShape
            )
            .border(
                width = 1.dp,
                color = DashlaneTheme.colors.borderNeutralQuietIdle,
                shape = CircleShape
            )
            .padding(all = 10.dp),
        token = IconTokens.groupOutlined,
        contentDescription = null,
        tint = DashlaneTheme.colors.textBrandQuiet
    )
}

@Composable
private fun ListTitle(text: String) {
    Text(
        text = text,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
        style = DashlaneTheme.typography.titleSupportingSmall,
        enforceAllCaps = true,
        color = DashlaneTheme.colors.textNeutralQuiet,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
private fun IndividualItem(individual: Individual, onSelectionChange: (Individual) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .setCheckboxSemantic(
                value = individual.selected,
                enabled = true,
            ) { onSelectionChange.invoke(individual) }
            .padding(start = 12.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = individual.selected)
        Spacer(modifier = Modifier.width(8.dp))
        ContactIcon(
            email = individual.username,
            modifier = Modifier.size(36.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = individual.username,
            style = DashlaneTheme.typography.bodyStandardRegular,
            color = DashlaneTheme.colors.textNeutralCatchy,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Preview
@Composable
fun PreviewShareBusinessCollection() {
    DashlanePreview {
        AvailableRecipientsList(
            viewData = ViewData(
                listOf(
                    UserGroup("1", "All", 324),
                    UserGroup("2", "Marketing", 34),
                    UserGroup("3", "Engineering", 16)
                ),
                listOf(
                    Individual("randomemail@provider.com"),
                    Individual("randomemail@provider.com"),
                    Individual("randomemail@provider.com"),
                    Individual("randomemail@provider.com"),
                    Individual("randomemail@provider.com"),
                    Individual("randomemail@provider.com"),
                    Individual("randomemail@provider.com")
                )
            ),
            showSharingButton = true,
            searchQuery = "",
            userInteractionListener = object : UserInteractionListener {
                override fun onGroupSelectionChange(group: UserGroup) {
                    
                }

                override fun onIndividualSelectionChange(individual: Individual) {
                    
                }

                override fun onShareClicked() {
                    
                }

                override fun onToggleSearch() {
                    
                }
            }
        )
    }
}