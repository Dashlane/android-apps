package com.dashlane.sharingcenter.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dashlane.design.component.BaseButtonBar
import com.dashlane.design.component.ButtonLayout
import com.dashlane.design.component.ButtonMedium
import com.dashlane.design.component.ListItem
import com.dashlane.design.component.Text
import com.dashlane.design.component.ThumbnailType
import com.dashlane.design.component.cardBackground
import com.dashlane.design.iconography.IconTokens
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.color.Intensity
import com.dashlane.design.theme.color.Mood
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.sharing.SharingContact
import com.dashlane.sharing_center.R
import com.dashlane.sharingcenter.SharingCenterEvent.PendingRequestEvent
import com.dashlane.sharingcenter.fabBottomPadding
import com.dashlane.sharingcenter.fabSize
import com.dashlane.ui.common.compose.components.EmptyContent
import com.dashlane.vault.item.VaultListItem
import com.dashlane.vault.item.VaultListItemState

@Suppress("LongMethod")
@Composable
internal fun RequestTabContent(
    itemInvites: List<Pair<SharingContact.ItemInvite, VaultListItemState>>,
    groupInvites: List<SharingContact.UserGroupInvite>,
    collectionInvites: List<SharingContact.CollectionInvite>,
    onPendingRequestEvent: (PendingRequestEvent) -> Unit,
) {
    if (itemInvites.isEmpty() && groupInvites.isEmpty() && collectionInvites.isEmpty()) {
        EmptyContent(
            iconToken = IconTokens.feedbackSuccessOutlined,
            title = stringResource(R.string.sharing_center_tab_pending_requests_content_empty_title),
            description = stringResource(R.string.sharing_center_tab_pending_requests_content_empty_description),
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = 16.dp,
                
                bottom = 16.dp + fabSize + fabBottomPadding,
                start = 16.dp,
                end = 16.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            items(items = groupInvites, key = { "content group - ${it.userGroup.groupId}" }) { groupInvite ->
                PendingRequestContent(
                    title = stringResource(R.string.sharing_center_pending_group_invite_title),
                    referrer = groupInvite.referrer,
                    listItem = {
                        ListItem(
                            modifier = Modifier
                                .animateItem()
                                .fillMaxWidth()
                                .padding(top = 8.dp, bottom = 8.dp),
                            thumbnailType = ThumbnailType.Icon(
                                token = IconTokens.groupOutlined,
                            ),
                            title = groupInvite.userGroup.name,
                            description = pluralStringResource(
                                R.plurals.sharing_center_group_member_count,
                                groupInvite.userGroup.users.size,
                                groupInvite.userGroup.users.size,
                            ),
                            onClick = null,
                        )
                    },
                    onDecline = {
                        onPendingRequestEvent(PendingRequestEvent.DeclineGroupInviteEvent(groupInvite))
                    },
                    onAccept = {
                        onPendingRequestEvent(PendingRequestEvent.AcceptGroupInviteEvent(groupInvite))
                    },
                )
            }
            items(items = collectionInvites, key = { "content collection - ${it.collection.uuid}" }) { collectionInvite ->
                PendingRequestContent(
                    title = stringResource(R.string.sharing_center_pending_collection_invite_title),
                    referrer = collectionInvite.referrer,
                    listItem = {
                        ListItem(
                            modifier = Modifier
                                .animateItem()
                                .fillMaxWidth()
                                .padding(top = 8.dp, bottom = 8.dp),
                            thumbnailType = ThumbnailType.Icon(
                                token = IconTokens.collectionOutlined,
                            ),
                            title = collectionInvite.collection.name,
                            description = stringResource(R.string.sharing_center_collection_item_count_hidden),
                            onClick = null,
                        )
                    },
                    onDecline = {
                        onPendingRequestEvent(
                            PendingRequestEvent.DeclineCollectionInviteEvent(
                                collectionInvite,
                            ),
                        )
                    },
                    onAccept = {
                        onPendingRequestEvent(
                            PendingRequestEvent.AcceptCollectionInviteEvent(
                                collectionInvite,
                            ),
                        )
                    },
                )
            }
            items(items = itemInvites, key = { "content item - ${it.second.id}" }) { (itemInvite, vaultListItemState) ->
                PendingRequestContent(
                    modifier = Modifier.animateItem(),
                    title = stringResource(R.string.sharing_center_pending_item_invite_title),
                    referrer = itemInvite.referrer,
                    listItem = { VaultListItem(item = vaultListItemState, onClick = null) },
                    onDecline = {
                        onPendingRequestEvent(PendingRequestEvent.DeclineItemInviteEvent(itemInvite, vaultListItemState.title))
                    },
                    onAccept = {
                        onPendingRequestEvent(PendingRequestEvent.AcceptItemInviteEvent(itemInvite, vaultListItemState.title))
                    },
                )
            }
        }
    }
}

@Composable
private fun PendingRequestContent(
    modifier: Modifier = Modifier,
    title: String,
    referrer: String,
    listItem: @Composable (() -> Unit),
    onDecline: () -> Unit,
    onAccept: () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .cardBackground(isTop = true, isBottom = true)
            .padding(all = 16.dp),
    ) {
        Text(
            text = title,
            style = DashlaneTheme.typography.titleBlockMedium,
            color = DashlaneTheme.colors.textNeutralCatchy,
        )
        Text(
            text = referrer,
            style = DashlaneTheme.typography.bodyReducedRegular,
            color = DashlaneTheme.colors.textNeutralStandard,
        )
        listItem()
        BaseButtonBar(
            modifier = Modifier.fillMaxWidth(),
            horizontalGapInDp = 8.dp,
            verticalGapInDp = 0.dp,
        ) {
            ButtonMedium(
                onClick = onAccept,
                layout = ButtonLayout.TextOnly(
                    text = stringResource(R.string.sharing_center_pending_invite_accept),
                ),
                intensity = Intensity.Quiet,
                mood = Mood.Positive,
            )
            ButtonMedium(
                onClick = onDecline,
                layout = ButtonLayout.TextOnly(
                    text = stringResource(R.string.sharing_center_pending_invite_decline),
                ),
                intensity = Intensity.Quiet,
                mood = Mood.Danger,
            )
        }
    }
}

@Preview
@Composable
private fun RequestTabContentEmptyPreview() {
    DashlanePreview {
        RequestTabContent(
            itemInvites = emptyList(),
            groupInvites = emptyList(),
            collectionInvites = emptyList(),
            onPendingRequestEvent = {},
        )
    }
}