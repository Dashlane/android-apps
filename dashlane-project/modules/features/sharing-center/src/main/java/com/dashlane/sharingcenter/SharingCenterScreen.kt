package com.dashlane.sharingcenter

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dashlane.design.component.DashlaneSnackbarWrapper
import com.dashlane.design.component.Icon
import com.dashlane.design.component.Tab
import com.dashlane.design.component.TabRow
import com.dashlane.design.component.Text
import com.dashlane.design.iconography.IconTokens
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.sharing_center.R
import com.dashlane.sharingcenter.ui.ContentTabContent
import com.dashlane.sharingcenter.ui.PeopleTabContent
import com.dashlane.sharingcenter.ui.RequestTabContent

internal val fabSize = 56.dp
internal val fabBottomPadding = 16.dp

private enum class SharingCenterTab(
    @StringRes val title: Int,
) {
    REQUESTS_TAB(R.string.sharing_center_tab_pending_requests),
    PEOPLE_TAB(R.string.sharing_center_tab_people),
    CONTENT_TAB(R.string.sharing_center_tab_content),
}

@Composable
fun SharingCenterScreen(
    viewModel: SharingCenterViewModel = viewModel(),
    goToNewShare: () -> Unit,
    goToSharedCollection: (id: String) -> Unit,
    goToItemDetail: (id: String, itemType: String) -> Unit,
) {
    val state by viewModel.stateFlow.viewState.collectAsStateWithLifecycle()
    val snackbarState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.sendEvent(SharingCenterEvent.ViewStarted)
        viewModel.stateFlow.sideEffect.collect { sideEffect ->
            when (sideEffect) {
                is SharingCenterState.SideEffect.ShowToast -> snackbarState.showSnackbar(sideEffect.message)
            }
        }
    }
    SharingCenterScreenContent(
        peopleData = state.peopleData,
        pendingRequestData = state.pendingRequestData,
        contentData = state.contentData,
        onCreateNewShareClicked = goToNewShare,
        onPendingRequestEvent = { viewModel.sendEvent(it) },
        onCollectionClicked = goToSharedCollection,
        onItemClicked = goToItemDetail,
    )

    Box(modifier = Modifier.fillMaxWidth()) {
        DashlaneSnackbarWrapper {
            SnackbarHost(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = fabSize + fabBottomPadding),
                hostState = snackbarState,
            )
        }
    }
}

@Suppress("LongMethod")
@Composable
private fun SharingCenterScreenContent(
    peopleData: SharingCenterPeopleData,
    pendingRequestData: SharingCenterPendingRequestData,
    contentData: SharingCenterContentData,
    onCreateNewShareClicked: () -> Unit,
    onPendingRequestEvent: (SharingCenterEvent.PendingRequestEvent) -> Unit,
    onCollectionClicked: (id: String) -> Unit,
    onItemClicked: (id: String, itemType: String) -> Unit,
) {
    Column {
        var selectedTab by rememberSaveable { mutableStateOf(SharingCenterTab.entries.first()) }
        TabRow(
            selectedTabIndex = selectedTab.ordinal,
            scrollable = false,
            containerColor = DashlaneTheme.colors.containerAgnosticNeutralStandard,
        ) {
            SharingCenterTab.entries.forEach {
                Tab(
                    selected = selectedTab == it,
                    onClick = { selectedTab = it },
                    text = stringResource(it.title),
                )
            }
        }
        when (selectedTab) {
            SharingCenterTab.REQUESTS_TAB -> {
                RequestTabContent(
                    itemInvites = pendingRequestData.itemInvites,
                    groupInvites = pendingRequestData.groupInvites,
                    collectionInvites = pendingRequestData.collectionInvites,
                    onPendingRequestEvent = onPendingRequestEvent,
                )
            }

            SharingCenterTab.PEOPLE_TAB -> {
                PeopleTabContent(
                    groups = peopleData.groups,
                    users = peopleData.users,
                )
            }

            SharingCenterTab.CONTENT_TAB -> {
                ContentTabContent(
                    collections = contentData.collections,
                    items = contentData.items,
                    onCollectionClicked = onCollectionClicked,
                    onItemClicked = onItemClicked,
                )
            }
        }
    }
    Box {
        ExtendedFloatingActionButton(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = fabBottomPadding)
                .height(fabSize),
            text = {
                Text(
                    text = stringResource(R.string.sharing_center_fab_new_share),
                    color = DashlaneTheme.colors.textInverseCatchy,
                    style = DashlaneTheme.typography.componentButtonStandard.copy(fontSize = 16.sp),
                )
            },
            icon = {
                Icon(
                    modifier = Modifier.size(20.dp),
                    token = IconTokens.actionShareOutlined,
                    contentDescription = null,
                    tint = DashlaneTheme.colors.textInverseCatchy,
                )
            },
            onClick = onCreateNewShareClicked,
            containerColor = DashlaneTheme.colors.containerExpressiveBrandCatchyIdle,
            contentColor = DashlaneTheme.colors.textInverseCatchy.value,
        )
    }
}

@Preview
@Composable
fun SharingCenterScreenContentPreview() {
    DashlanePreview {
        SharingCenterScreenContent(
            peopleData = SharingCenterPeopleData(
                groups = emptyList(),
                users = emptyList(),
            ),
            pendingRequestData = SharingCenterPendingRequestData(
                itemInvites = emptyList(),
                groupInvites = emptyList(),
                collectionInvites = emptyList(),
            ),
            contentData = SharingCenterContentData(
                collections = emptyList(),
                items = emptyList(),
            ),
            onCreateNewShareClicked = {},
            onPendingRequestEvent = {},
            onCollectionClicked = {},
            onItemClicked = { _, _ -> },
        )
    }
}