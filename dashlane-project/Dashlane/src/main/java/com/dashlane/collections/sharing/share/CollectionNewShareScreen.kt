package com.dashlane.collections.sharing.share

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dashlane.R
import com.dashlane.collections.CollectionLoading
import com.dashlane.collections.sharing.CollectionSharingAppBarListener
import com.dashlane.collections.sharing.CollectionSharingListTitle
import com.dashlane.collections.sharing.CollectionSharingTopAppBar
import com.dashlane.collections.sharing.CollectionSharingViewState
import com.dashlane.collections.sharing.CollectionSharingViewState.Individual
import com.dashlane.collections.sharing.CollectionSharingViewState.Loading
import com.dashlane.collections.sharing.CollectionSharingViewState.SharingFailed
import com.dashlane.collections.sharing.CollectionSharingViewState.SharingRestricted
import com.dashlane.collections.sharing.CollectionSharingViewState.SharingSuccess
import com.dashlane.collections.sharing.CollectionSharingViewState.ShowList
import com.dashlane.collections.sharing.CollectionSharingViewState.UserGroup
import com.dashlane.collections.sharing.CollectionSharingViewState.ViewData
import com.dashlane.design.component.ButtonLarge
import com.dashlane.design.component.ButtonLayout
import com.dashlane.design.component.InfoboxMedium
import com.dashlane.design.component.ListItem
import com.dashlane.design.component.Text
import com.dashlane.design.component.ThumbnailType
import com.dashlane.design.iconography.IconTokens
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.color.Intensity
import com.dashlane.design.theme.color.Mood.Brand
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.design.theme.typography.withSearchHighlight
import com.dashlane.util.MD5Hash

const val COLLECTION_ROLE_SEPARATOR = " • "

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
                is ShowList ->
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
                is SharingRestricted ->
                    userNavigationListener.onSharingRestricted()
                
                else -> Unit
            }
        }
    }
}

@Composable
private fun CollectionNewShareTopBar(
    uiState: CollectionSharingViewState,
    viewData: ViewData,
    searchQuery: MutableState<String>,
    isSearching: MutableState<Boolean>,
    viewModel: CollectionsNewShareViewModel,
    userNavigationListener: UserNavigationListener
) {
    CollectionSharingTopAppBar(
        title = stringResource(id = R.string.collection_new_share_title),
        searchHint = stringResource(R.string.collection_new_share_search_hint),
        uiState = uiState,
        viewData = viewData,
        searchQuery = searchQuery,
        isSearching = isSearching,
        listener = object : CollectionSharingAppBarListener {
            override fun onCloseClicked() {
                userNavigationListener.onCloseClicked()
            }

            override fun onToggleSearch() {
                viewModel.onToggleSearch()
            }
        }
    )
}

@Suppress("LongMethod")
@Composable
private fun AvailableRecipientsList(
    viewData: ViewData,
    showSharingButton: Boolean,
    searchQuery: String,
    userInteractionListener: UserInteractionListener
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        if (searchQuery.isEmpty()) {
            if (viewData.showSharingLimit) {
                InfoboxSharingLimit()
            } else {
                InfoboxPermission()
            }
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
                    CollectionSharingListTitle(stringResource(R.string.collection_new_share_groups_section_title))
                }
                items(filteredUserGroups) { group ->
                    UserGroupItem(
                        group = group,
                        searchQuery = searchQuery,
                        onSelectionChange = userInteractionListener::onGroupSelectionChange
                    )
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
                item { CollectionSharingListTitle(stringResource(R.string.collection_new_share_individuals_section_title)) }
                items(filteredIndividuals) { individual ->
                    IndividualItem(
                        individual = individual,
                        searchQuery = searchQuery,
                        onSelectionChange = userInteractionListener::onIndividualSelectionChange
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
private fun InfoboxSharingLimit() {
    InfoboxMedium(
        modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 8.dp),
        mood = Brand,
        title = stringResource(id = R.string.collections_limiter_approaching_title),
        description = stringResource(id = R.string.collections_limiter_approaching_description),
    )
}

@Composable
private fun InfoboxPermission() {
    InfoboxMedium(
        modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 8.dp),
        mood = Brand,
        title = stringResource(id = R.string.collection_new_share_infobox_title_roles),
        description = stringResource(id = R.string.collection_new_share_infobox_description_roles)
    )
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
private fun UserGroupItem(
    group: UserGroup,
    searchQuery: String,
    onSelectionChange: (UserGroup) -> Unit
) {
    val statusText = pluralStringResource(
        id = R.plurals.collections_new_share_group_members,
        count = group.membersCount,
        formatArgs = arrayOf(group.membersCount)
    ) + COLLECTION_ROLE_SEPARATOR + stringResource(id = R.string.collection_role_manager)
    ListItem(
        modifier = Modifier.padding(horizontal = 8.dp),
        checked = group.selected,
        thumbnailType = ThumbnailType.Icon(token = IconTokens.groupOutlined),
        title = group.name.withSearchHighlight(match = searchQuery, ignoreCase = true),
        description = AnnotatedString(statusText),
        onClick = { onSelectionChange(group) },
    )
}

@Composable
private fun IndividualItem(
    individual: Individual,
    searchQuery: String,
    onSelectionChange: (Individual) -> Unit
) {
    val url = remember(individual.username) {
        "https://www.gravatar.com/avatar/${MD5Hash.hash(individual.username)}?s=200&r=pg&d=404"
    }
    ListItem(
        modifier = Modifier.padding(horizontal = 8.dp),
        checked = individual.selected,
        thumbnailType = ThumbnailType.User.Single(url = url),
        title = individual.username.withSearchHighlight(match = searchQuery, ignoreCase = true),
        description = AnnotatedString(stringResource(id = R.string.collection_role_manager)),
        onClick = { onSelectionChange(individual) }
    )
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