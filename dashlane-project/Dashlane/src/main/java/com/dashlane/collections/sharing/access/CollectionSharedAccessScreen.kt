package com.dashlane.collections.sharing.access

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.invisibleToUser
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
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
import com.dashlane.collections.sharing.CollectionSharingViewState.ConfirmRevoke
import com.dashlane.collections.sharing.CollectionSharingViewState.Individual
import com.dashlane.collections.sharing.CollectionSharingViewState.Loading
import com.dashlane.collections.sharing.CollectionSharingViewState.MyselfRevoked
import com.dashlane.collections.sharing.CollectionSharingViewState.ShowList
import com.dashlane.collections.sharing.CollectionSharingViewState.UserGroup
import com.dashlane.collections.sharing.CollectionSharingViewState.ViewData
import com.dashlane.collections.sharing.share.COLLECTION_ROLE_SEPARATOR
import com.dashlane.design.component.ButtonLayout.TextOnly
import com.dashlane.design.component.ButtonMedium
import com.dashlane.design.component.Dialog
import com.dashlane.design.component.InfoboxMedium
import com.dashlane.design.component.ListItem
import com.dashlane.design.component.ListItemActions
import com.dashlane.design.component.Text
import com.dashlane.design.component.ThumbnailType
import com.dashlane.design.iconography.IconTokens
import com.dashlane.design.theme.color.Intensity
import com.dashlane.design.theme.color.Mood
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.design.theme.typography.withSearchHighlight
import com.dashlane.server.api.endpoints.sharinguserdevice.Permission
import com.dashlane.util.MD5Hash

var totalMemberCountBeforeRevoke = 0

@Suppress("LongMethod")
@Composable
internal fun CollectionSharedAccessScreen(
    viewModel: CollectionSharedAccessViewModel = viewModel(),
    userInteractionListener: UserInteractionListener
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val viewData = uiState.viewData
    val searchQuery = remember { mutableStateOf("") }
    val isSearching = remember { mutableStateOf(false) }
    Scaffold(
        topBar = {
            CollectionSharedAccessTopBar(
                uiState,
                viewData,
                searchQuery,
                isSearching,
                viewModel,
                userInteractionListener
            )
        }
    ) {
        Box(modifier = Modifier.padding(it)) {
            when (uiState) {
                is Loading -> CollectionLoading()
                is ShowList, is ConfirmRevoke -> {
                    Column {
                        if (!viewData.isAdmin) EditorPermissionInfobox()
                        SharedGroupsAndUsersList(
                            viewData,
                            viewModel.login,
                            searchQuery.value,
                            revokeGroupAction = { group ->
                                viewModel.onRevokeClicked(group)
                            },
                            revokeUserAction = { individual ->
                                viewModel.onRevokeClicked(individual)
                            }
                        )
                    }
                    if (totalMemberCountBeforeRevoke != 0) {
                        val newMembersCount = viewData.individuals.size + viewData.userGroups.size
                        if (totalMemberCountBeforeRevoke != newMembersCount) {
                            userInteractionListener.onRevokeSuccess()
                        } else {
                            userInteractionListener.onRevokeFailed()
                        }
                        totalMemberCountBeforeRevoke = 0
                    }
                    val revokeState = (uiState as? ConfirmRevoke) ?: return@Box
                    RevokeConfirmDialog(
                        revokeState.userToRevoke,
                        revokeState.groupToRevoke,
                        revokeUserAction = { individual ->
                            totalMemberCountBeforeRevoke =
                                viewData.individuals.size + viewData.userGroups.size
                            viewModel.revokeMember(userId = individual.username)
                        },
                        revokeGroupAction = { group ->
                            totalMemberCountBeforeRevoke =
                                viewData.individuals.size + viewData.userGroups.size
                            viewModel.revokeMember(groupId = group.groupId)
                        },
                        dismissAction = {
                            totalMemberCountBeforeRevoke = 0
                            viewModel.onRevokeCancelClicked()
                        }
                    )
                }
                is MyselfRevoked -> userInteractionListener.onCloseClicked()
                
                else -> Unit
            }
        }
    }
}

@Composable
private fun EditorPermissionInfobox() {
    InfoboxMedium(
        modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 8.dp),
        mood = Mood.Brand,
        title = stringResource(id = R.string.collection_shared_access_infobox_title_roles),
        description = stringResource(id = R.string.collection_shared_access_infobox_description_roles)
    )
}

@Composable
private fun CollectionSharedAccessTopBar(
    uiState: CollectionSharingViewState,
    viewData: ViewData,
    searchQuery: MutableState<String>,
    isSearching: MutableState<Boolean>,
    viewModel: CollectionSharedAccessViewModel,
    userInteractionListener: UserInteractionListener
) {
    CollectionSharingTopAppBar(
        title = stringResource(id = R.string.collection_shared_access_title),
        searchHint = stringResource(id = R.string.collection_new_share_search_hint),
        uiState = uiState,
        viewData = viewData,
        searchQuery = searchQuery,
        isSearching = isSearching,
        listener = object : CollectionSharingAppBarListener {
            override fun onCloseClicked() {
                userInteractionListener.onCloseClicked()
            }

            override fun onToggleSearch() {
                viewModel.onToggleSearch()
            }
        }
    )
}

@Composable
private fun SharedGroupsAndUsersList(
    viewData: ViewData,
    login: String,
    searchQuery: String,
    revokeGroupAction: (UserGroup) -> Unit,
    revokeUserAction: (Individual) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxWidth()) {
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
                val revokeAction =
                    if (viewData.individuals.isEmpty() && viewData.userGroups.size == 1) {
                        null
                    } else {
                        revokeGroupAction
                    }
                UserGroupItem(
                    group = group,
                    canRevoke = viewData.isAdmin,
                    searchQuery = searchQuery,
                    revokeGroupAction = revokeAction
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
                val revokeAction = if (individual.username == login) null else revokeUserAction
                IndividualItem(
                    individual = individual,
                    canRevoke = viewData.isAdmin,
                    searchQuery = searchQuery,
                    revokeUserAction = revokeAction
                )
            }
        }
    }
}

@Suppress("LongMethod")
@Composable
private fun UserGroupItem(
    group: UserGroup,
    canRevoke: Boolean,
    searchQuery: String,
    revokeGroupAction: ((UserGroup) -> Unit)?
) {
    val isAdmin = remember(group.permission) { group.permission == Permission.ADMIN }
    val groupMemberCount = pluralStringResource(
        id = R.plurals.collections_new_share_group_members,
        count = group.membersCount,
        formatArgs = arrayOf(group.membersCount)
    )
    val roleStatus = getRoleStatus(isAdmin)
    val statusText = remember(groupMemberCount, roleStatus) {
        groupMemberCount + COLLECTION_ROLE_SEPARATOR + roleStatus
    }
    val revokeLabel = stringResource(id = R.string.collection_shared_access_revoke_button_accessibility)
    val actions = remember(revokeGroupAction) {
        if (revokeGroupAction != null) {
            object : ListItemActions {
                @OptIn(ExperimentalComposeUiApi::class)
                @Composable
                override fun Content() {
                    ButtonMedium(
                        modifier = Modifier.semantics { invisibleToUser() },
                        onClick = {
                            if (canRevoke) revokeGroupAction(group)
                        },
                        layout = TextOnly(text = revokeLabel),
                        mood = Mood.Warning,
                        intensity = Intensity.Supershy,
                        enabled = canRevoke
                    )
                }

                override fun getCustomAccessibilityActions(): List<CustomAccessibilityAction> {
                    return if (canRevoke) {
                        listOf(
                            CustomAccessibilityAction(
                                label = revokeLabel,
                                action = {
                                    revokeGroupAction(group)
                                    true
                                },
                            )
                        )
                    } else {
                        emptyList()
                    }
                }
            }
        } else {
            null
        }
    }
    ListItem(
        modifier = Modifier.padding(horizontal = 8.dp),
        thumbnailType = ThumbnailType.Icon(token = IconTokens.groupOutlined),
        title = group.name.withSearchHighlight(match = searchQuery, ignoreCase = true),
        description = AnnotatedString(statusText),
        actions = actions,
        onClick = null,
    )
}

@Composable
private fun IndividualItem(
    individual: Individual,
    canRevoke: Boolean,
    searchQuery: String,
    revokeUserAction: ((Individual) -> Unit)?
) {
    val url = remember(individual.username) {
        "https://www.gravatar.com/avatar/${MD5Hash.hash(individual.username)}?s=200&r=pg&d=404"
    }
    val statusText = getIndividualStatus(
        individual = individual,
        revokeUserAction = revokeUserAction
    )
    val revokeLabel = stringResource(id = R.string.collection_shared_access_revoke_button_accessibility)
    val actions = remember(revokeUserAction) {
        if (revokeUserAction != null) {
            object : ListItemActions {
                @OptIn(ExperimentalComposeUiApi::class)
                @Composable
                override fun Content() {
                    ButtonMedium(
                        modifier = Modifier.semantics { invisibleToUser() },
                        onClick = {
                            if (canRevoke) revokeUserAction(individual)
                        },
                        layout = TextOnly(text = revokeLabel),
                        mood = Mood.Warning,
                        intensity = Intensity.Supershy,
                        enabled = canRevoke
                    )
                }

                override fun getCustomAccessibilityActions(): List<CustomAccessibilityAction> {
                    return if (canRevoke) {
                        listOf(
                            CustomAccessibilityAction(
                                label = revokeLabel,
                                action = {
                                    revokeUserAction(individual)
                                    true
                                },
                            )
                        )
                    } else {
                        emptyList()
                    }
                }
            }
        } else {
            null
        }
    }
    ListItem(
        modifier = Modifier.padding(horizontal = 8.dp),
        thumbnailType = ThumbnailType.User.Single(url = url),
        title = individual.username.withSearchHighlight(match = searchQuery, ignoreCase = true),
        description = AnnotatedString(statusText),
        actions = actions,
        onClick = null,
    )
}

@Composable
private fun getIndividualStatus(
    individual: Individual,
    revokeUserAction: ((Individual) -> Unit)?
): String {
    val isAdmin = remember(individual.permission) { individual.permission == Permission.ADMIN }
    val roleStatus = getRoleStatus(isAdmin)
    val pendingStatus = stringResource(id = R.string.collection_shared_access_item_invitation_pending)
    val removeMyselfStatus = stringResource(id = R.string.collection_shared_access_item_member_myself)
    val description = remember(individual.accepted, revokeUserAction) {
        when {
            !individual.accepted -> {
                roleStatus + COLLECTION_ROLE_SEPARATOR + pendingStatus
            }
            revokeUserAction == null -> {
                roleStatus + COLLECTION_ROLE_SEPARATOR + removeMyselfStatus
            }
            else -> roleStatus
        }
    }
    return description
}

@Composable
private fun getRoleStatus(isAdmin: Boolean) = if (isAdmin) {
    stringResource(id = R.string.collection_role_manager)
} else {
    stringResource(id = R.string.collection_role_editor)
}

@Composable
private fun RevokeConfirmDialog(
    individual: Individual?,
    group: UserGroup?,
    revokeUserAction: (Individual) -> Unit,
    revokeGroupAction: (UserGroup) -> Unit,
    dismissAction: () -> Unit
) {
    val name = individual?.username ?: group?.name ?: ""
    Dialog(
        onDismissRequest = {
            dismissAction.invoke()
        },
        title = stringResource(id = R.string.collection_shared_access_revoke_dialog_title),
        description = {
            Text(
                text = stringResource(
                    id = R.string.collection_shared_access_revoke_dialog_text,
                    name
                )
            )
        },
        mainActionLayout = TextOnly(
            stringResource(R.string.collection_shared_access_revoke_dialog_positive_button)
        ),
        mainActionClick = {
            individual?.let { revokeUserAction.invoke(it) }
            group?.let { revokeGroupAction.invoke(it) }
        },
        additionalActionLayout = TextOnly(
            stringResource(R.string.collection_shared_access_revoke_dialog_negative_button)
        ),
        additionalActionClick = {
            dismissAction.invoke()
        },
        isDestructive = true
    )
}

@Preview
@Composable
private fun PreviewSharedUsersAndGroups() {
    DashlanePreview {
        SharedGroupsAndUsersList(
            viewData = ViewData(
                userGroups = listOf(
                    UserGroup("1", "All", 324),
                    UserGroup("2", "Marketing", 34),
                    UserGroup("3", "Engineering", 16)
                ),
                individuals = listOf(
                    Individual("randomemail@provider.com"),
                    Individual("randomemail@provider.com", accepted = false),
                    Individual("randomemail@provider.com"),
                    Individual("randomemail@provider.com"),
                    Individual("randomemail@provider.com"),
                    Individual("randomemail@provider.com"),
                    Individual("randomemail@provider.com")
                ),
                isAdmin = true,
            ),
            login = "randomemail@provider.com",
            searchQuery = "",
            revokeGroupAction = {
                
            },
            revokeUserAction = {
                
            }
        )
    }
}