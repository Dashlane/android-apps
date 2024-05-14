package com.dashlane.collections.sharing.access

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dashlane.R
import com.dashlane.collections.CollectionLoading
import com.dashlane.collections.sharing.CollectionSharingAppBarListener
import com.dashlane.collections.sharing.CollectionSharingGroupIcon
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
import com.dashlane.design.component.Icon
import com.dashlane.design.component.InfoboxMedium
import com.dashlane.design.component.Text
import com.dashlane.design.iconography.IconTokens
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.color.Intensity
import com.dashlane.design.theme.color.Mood
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.server.api.endpoints.sharinguserdevice.Permission
import com.dashlane.ui.widgets.compose.contact.ContactIcon

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
                        if (viewData.showRoles && !viewData.isAdmin) EditorPermissionInfobox()
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
                UserGroupItem(group, viewData.isAdmin, viewData.showRoles, revokeAction)
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
                IndividualItem(individual, viewData.isAdmin, viewData.showRoles, revokeAction)
            }
        }
    }
}

@Suppress("LongMethod")
@Composable
private fun UserGroupItem(
    group: UserGroup,
    canRevoke: Boolean,
    showRoles: Boolean,
    revokeGroupAction: ((UserGroup) -> Unit)?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 8.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val isAdmin = group.permission == Permission.ADMIN
        CollectionSharingGroupIcon()
        Spacer(modifier = Modifier.width(12.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Text(
                text = group.name,
                style = DashlaneTheme.typography.bodyStandardRegular,
                color = DashlaneTheme.colors.textNeutralCatchy,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            val statusText = if (!showRoles) {
                pluralStringResource(
                    id = R.plurals.collections_new_share_group_members,
                    count = group.membersCount,
                    formatArgs = arrayOf(group.membersCount)
                )
            } else {
                pluralStringResource(
                    id = R.plurals.collections_new_share_group_members,
                    count = group.membersCount,
                    formatArgs = arrayOf(group.membersCount)
                ) + COLLECTION_ROLE_SEPARATOR + if (isAdmin) {
                    stringResource(id = R.string.collection_role_manager)
                } else {
                    stringResource(id = R.string.collection_role_editor)
                }
            }
            Text(
                text = statusText,
                style = DashlaneTheme.typography.bodyReducedRegular,
                color = DashlaneTheme.colors.textNeutralQuiet,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (revokeGroupAction == null) return@Row
        ButtonMedium(
            onClick = {
                if (canRevoke) revokeGroupAction.invoke(group)
            },
            layout = TextOnly(
                text = stringResource(id = R.string.collection_shared_access_revoke_button_accessibility)
            ),
            mood = Mood.Warning,
            intensity = Intensity.Catchy,
            enabled = canRevoke
        )
    }
}

@Composable
private fun IndividualItem(
    individual: Individual,
    canRevoke: Boolean,
    showRoles: Boolean,
    revokeUserAction: ((Individual) -> Unit)?
) {
    val isAdmin = individual.permission == Permission.ADMIN
    val isMyself = revokeUserAction == null
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 8.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ContactIcon(
            email = individual.username,
            modifier = Modifier.size(36.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Text(
                text = individual.username,
                style = DashlaneTheme.typography.bodyStandardRegular,
                color = DashlaneTheme.colors.textNeutralCatchy,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            when {
                !individual.accepted -> PendingStatus(showRoles, isAdmin)
                isMyself -> RemoveMyselfStatus(isAdmin, showRoles)
                showRoles -> Role(isAdmin)
            }
        }
        if (isMyself) return@Row
        ButtonMedium(
            onClick = {
                if (canRevoke) revokeUserAction?.invoke(individual)
            },
            layout = TextOnly(
                text = stringResource(id = R.string.collection_shared_access_revoke_button_accessibility)
            ),
            mood = Mood.Warning,
            intensity = Intensity.Catchy,
            enabled = canRevoke
        )
    }
}

@Composable
private fun RemoveMyselfStatus(isAdmin: Boolean, showRoles: Boolean) {
    val text = if (!showRoles) {
        stringResource(id = R.string.collection_shared_access_item_member_myself)
    } else {
        val role = if (isAdmin) {
            stringResource(id = R.string.collection_role_manager)
        } else {
            stringResource(id = R.string.collection_role_editor)
        }
        role + COLLECTION_ROLE_SEPARATOR +
            stringResource(id = R.string.collection_shared_access_item_member_myself)
    }
    Text(
        text = text,
        style = DashlaneTheme.typography.bodyReducedRegular,
        color = DashlaneTheme.colors.textNeutralQuiet,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
private fun PendingStatus(showRoles: Boolean, isAdmin: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            token = IconTokens.timeOutlined,
            contentDescription = null,
            tint = DashlaneTheme.colors.textWarningQuiet,
            modifier = Modifier.size(12.dp)
        )
        Text(
            modifier = Modifier.padding(start = 2.dp),
            text = stringResource(id = R.string.collection_shared_access_item_invitation_pending),
            style = DashlaneTheme.typography.bodyReducedRegular,
            color = DashlaneTheme.colors.textWarningQuiet,
            maxLines = 1
        )
        if (!showRoles) return@Row
        val role = COLLECTION_ROLE_SEPARATOR + if (isAdmin) {
            stringResource(id = R.string.collection_role_manager)
        } else {
            stringResource(id = R.string.collection_role_editor)
        }
        Text(
            text = role,
            style = DashlaneTheme.typography.bodyReducedRegular,
            color = DashlaneTheme.colors.textNeutralQuiet,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun Role(isAdmin: Boolean) {
    val role = if (isAdmin) {
        stringResource(id = R.string.collection_role_manager)
    } else {
        stringResource(id = R.string.collection_role_editor)
    }
    Text(
        text = role,
        style = DashlaneTheme.typography.bodyReducedRegular,
        color = DashlaneTheme.colors.textNeutralQuiet,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
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
fun PreviewSharedUsersAndGroups() {
    DashlanePreview {
        SharedGroupsAndUsersList(
            viewData = ViewData(
                listOf(
                    UserGroup("1", "All", 324),
                    UserGroup("2", "Marketing", 34),
                    UserGroup("3", "Engineering", 16)
                ),
                listOf(
                    Individual("randomemail@provider.com"),
                    Individual("randomemail@provider.com", accepted = false),
                    Individual("randomemail@provider.com"),
                    Individual("randomemail@provider.com"),
                    Individual("randomemail@provider.com"),
                    Individual("randomemail@provider.com"),
                    Individual("randomemail@provider.com")
                )
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