package com.dashlane.sharingcenter.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dashlane.design.component.ListItem
import com.dashlane.design.component.ThumbnailType
import com.dashlane.design.component.cardBackground
import com.dashlane.design.iconography.IconTokens
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.sharing.SharingContact
import com.dashlane.sharing_center.R
import com.dashlane.sharingcenter.fabBottomPadding
import com.dashlane.sharingcenter.fabSize
import com.dashlane.ui.common.compose.components.EmptyContent

@Suppress("LongMethod")
@Composable
internal fun PeopleTabContent(
    groups: List<SharingContact.UserGroup>,
    users: List<SharingContact.User>,
) {
    if (groups.isEmpty() && users.isEmpty()) {
        EmptyContent(
            iconToken = IconTokens.usersOutlined,
            title = stringResource(R.string.sharing_center_tab_people_content_empty_title),
            description = stringResource(R.string.sharing_center_tab_people_content_empty_description),
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                
                top = 0.dp,
                
                bottom = 16.dp + fabSize + fabBottomPadding,
                start = 16.dp,
                end = 16.dp,
            ),
        ) {
            if (groups.isNotEmpty()) {
                item(key = "Header-group-section") {
                    SectionHeader(
                        modifier = Modifier.padding(top = 16.dp),
                        title = stringResource(R.string.sharing_center_usergroups_section_header),
                    )
                }
                items(
                    items = groups,
                    key = { group -> "groupID - ${group.groupId}" },
                ) { group ->
                    UserGroupItem(
                        modifier = Modifier.animateItem(),
                        group = group,
                    )
                }
                item(key = "group-section-card-bottom") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .cardBackground(isTop = false, isBottom = true),
                    )
                }
            }
            if (users.isNotEmpty()) {
                item(key = "Header-individual-section") {
                    SectionHeader(
                        modifier = Modifier.padding(top = 16.dp),
                        title = stringResource(R.string.sharing_center_users_section_header),
                    )
                }
                items(
                    items = users,
                    key = { user -> "username - ${user.name}" },
                ) { user ->
                    UserItem(
                        modifier = Modifier.animateItem(),
                        user = user,
                    )
                }
                item(key = "user-section-card-bottom") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .cardBackground(isTop = false, isBottom = true),
                    )
                }
            }
        }
    }
}

@Composable
private fun UserItem(
    modifier: Modifier = Modifier,
    user: SharingContact.User,
) {
    ListItemWithBackground(
        modifier = modifier,
        title = user.name,
        description = pluralStringResource(
            id = R.plurals.sharing_center_item_shared_count,
            count = user.count,
            user.count,
        ),
        thumbnailType = ThumbnailType.Icon(
            token = IconTokens.usersOutlined,
        ),
    ) {
        
    }
}

@Composable
private fun UserGroupItem(
    modifier: Modifier = Modifier,
    group: SharingContact.UserGroup,
) {
    val memberCountLabel = pluralStringResource(
        id = R.plurals.sharing_center_group_member_count,
        count = group.memberCount,
        group.memberCount,
    )
    val itemCountLabel = pluralStringResource(
        id = R.plurals.sharing_center_item_shared_count,
        count = group.itemCount,
        group.itemCount,
    )
    ListItemWithBackground(
        modifier = modifier,
        title = group.name,
        description = "$memberCountLabel • $itemCountLabel",
        thumbnailType = ThumbnailType.Icon(
            token = IconTokens.groupOutlined,
        ),
    ) {
        
    }
}

@Composable
private fun ListItemWithBackground(
    modifier: Modifier = Modifier,
    title: String,
    description: String,
    thumbnailType: ThumbnailType,
    onClick: (() -> Unit)?,
) {
    ListItem(
        modifier = modifier
            .fillMaxWidth()
            .cardBackground(isTop = false, isBottom = false)
            .padding(
                top = 0.dp,
                bottom = 0.dp,
                start = 8.dp,
                end = 8.dp,
            ),
        thumbnailType = thumbnailType,
        title = title,
        description = description,
        onClick = onClick,
    )
}

@Preview
@Composable
private fun PeopleTabContentEmptyPreview() {
    DashlanePreview {
        PeopleTabContent(
            groups = emptyList(),
            users = emptyList(),
        )
    }
}