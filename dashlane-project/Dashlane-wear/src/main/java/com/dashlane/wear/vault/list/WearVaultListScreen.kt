package com.dashlane.wear.vault.list

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.ScalingLazyListScope
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.ui.tooling.preview.WearPreviewSmallRound
import com.dashlane.design.iconography.IconTokens
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.wear.R
import com.dashlane.wear.ui.DashlaneWearPreview
import com.dashlane.wear.ui.component.Emphasis
import com.dashlane.wear.ui.component.WearChip
import com.dashlane.wear.ui.component.WearIcon
import com.dashlane.wear.ui.component.WearText
import com.dashlane.wear.ui.screen.WearContentWrapper
import com.dashlane.wear.ui.screen.WearTextContentScreen
import com.dashlane.wear.vault.WearVaultItem

@Composable
fun WearVaultListScreen(
    viewModel: WearVaultListViewModel,
    onGoToSettings: () -> Unit,
    onGoToItemDetails: (String) -> Unit,
    onOpenSupportedServicesOnMobile: (Intent) -> Unit,
) {
    LaunchedEffect(viewModel) {
        
        viewModel.onViewStarted()
        viewModel.stateFlow.sideEffect.collect { sideEffect ->
            when (sideEffect) {
                is WearVaultState.SideEffect.GoToItem -> onGoToItemDetails(sideEffect.itemId)
                is WearVaultState.SideEffect.OpenSupportedServices -> onOpenSupportedServicesOnMobile(sideEffect.mobileIntent)
                WearVaultState.SideEffect.GoToSettings -> onGoToSettings()
            }
        }
    }

    val uiState by viewModel.stateFlow.viewState.collectAsStateWithLifecycle()
    val error = uiState.error

    if (error != null) {
        WearTextContentScreen(text = error)
    } else {
        WearVaultListScreenContent(
            items = uiState.items,
            isLoading = uiState.isLoading,
            showSupportedServicesAction = uiState.showSupportedServicesAction,
            onItemClick = viewModel::onItemClick,
            onCheckServicesClick = viewModel::onCheckSupportedServicesClick,
            onSettingClick = viewModel::onSettingClick,
        )
    }
}

@Suppress("LongMethod")
@Composable
fun WearVaultListScreenContent(
    modifier: Modifier = Modifier,
    items: List<WearVaultItem>,
    showSupportedServicesAction: Boolean,
    onItemClick: (String) -> Unit,
    onCheckServicesClick: () -> Unit,
    onSettingClick: () -> Unit,
    isLoading: Boolean,
) {
    val scalingListState = rememberScalingLazyListState(initialCenterItemScrollOffset = 1)

    WearContentWrapper(
        isLogoVisible = true,
        scrollableState = scalingListState,
        positionIndicator = {
            PositionIndicator(scalingLazyListState = scalingListState)
        },
    ) { contentPadding ->
        ScalingLazyColumn(
            modifier = modifier.padding(horizontal = 16.dp),
            state = scalingListState,
            autoCentering = null,
            contentPadding = PaddingValues(
                top = contentPadding.calculateTopPadding(),
                bottom = 48.dp,
            ),
        ) {
            item(key = "ItemSectionHeader") {
                WearText(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    text = stringResource(R.string.vault_filter_passkeys_passwords),
                    style = DashlaneTheme.typography.bodyReducedRegular.copy(fontWeight = FontWeight.Bold),
                    color = DashlaneTheme.colors.textInverseQuiet,
                    textAlign = TextAlign.Center,
                )
            }
            item(key = "SettingsItem") {
                WearChip(
                    modifier = Modifier.fillMaxWidth(),
                    icon = {
                        WearIcon(
                            token = IconTokens.settingsOutlined,
                            contentDescription = "",
                        )
                    },
                    label = stringResource(R.string.vault_settings_item),
                    emphasis = Emphasis.Medium,
                    onClick = onSettingClick,
                )
            }
            item(key = "Spacer") {
                Spacer(modifier = Modifier.size(4.dp))
            }
            if (items.isNotEmpty()) {
                itemsContent(items = items, onItemClick = onItemClick)
            } else {
                emptyVaultContent(
                    showMobileAction = showSupportedServicesAction,
                    onCheckServicesClick = onCheckServicesClick,
                )
            }
        }

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

fun ScalingLazyListScope.emptyVaultContent(
    showMobileAction: Boolean,
    onCheckServicesClick: () -> Unit,
) {
    item("item_empty_state") {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            WearText(
                text = stringResource(R.string.vault_empty_state_title),
                textAlign = TextAlign.Center,
                style = DashlaneTheme.typography.bodyReducedRegular,
                color = DashlaneTheme.colors.textInverseCatchy,
                maxLines = 2,
            )
            WearText(
                text = stringResource(R.string.vault_empty_state_content),
                textAlign = TextAlign.Center,
                style = DashlaneTheme.typography.bodyReducedRegular,
                color = DashlaneTheme.colors.textInverseCatchy,
                maxLines = 10,
            )
            if (showMobileAction) {
                WearChip(
                    label = stringResource(R.string.vault_empty_state_mobile_redirection),
                    labelMaxLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                    icon = {
                        WearIcon(
                            token = IconTokens.itemPhoneMobileOutlined,
                            contentDescription = null,
                        )
                    },
                    emphasis = Emphasis.Medium,
                    onClick = onCheckServicesClick,
                )
            }
        }
    }
}

private fun ScalingLazyListScope.itemsContent(
    items: List<WearVaultItem>,
    onItemClick: (String) -> Unit,
) {
    items(items) { item ->
        WearVaultItem(
            modifier = Modifier.fillMaxWidth(),
            url = item.url,
            title = item.title,
            content = item.username,
            isPasskey = item.isPasskey,
            onClick = { onItemClick(item.id) },
        )
    }
}

@WearPreviewSmallRound
@Composable
private fun WearVaultListScreenContentPreview() {
    DashlaneWearPreview {
        WearVaultListScreenContent(
            items = listOf(
                WearVaultItem(
                    id = "",
                    title = "Spotify",
                    username = "",
                    url = "",
                    urlForUi = "",
                    isPasskey = true,
                ),
                WearVaultItem(
                    id = "",
                    title = "passkeys.io",
                    username = "randomemail@provider.com",
                    url = "https://passkeys.io",
                    urlForUi = "passkeys.io",
                    isPasskey = false,
                ),
            ),
            isLoading = true,
            showSupportedServicesAction = false,
            onItemClick = {},
            onCheckServicesClick = {},
            onSettingClick = {},
        )
    }
}

@WearPreviewSmallRound
@Composable
private fun WearVaultListScreenEmptyStatePreview() {
    DashlaneWearPreview {
        WearVaultListScreenContent(
            items = emptyList(),
            isLoading = false,
            showSupportedServicesAction = true,
            onItemClick = {},
            onCheckServicesClick = {},
            onSettingClick = {},
        )
    }
}