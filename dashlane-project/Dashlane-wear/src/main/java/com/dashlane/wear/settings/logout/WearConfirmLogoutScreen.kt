package com.dashlane.wear.settings.logout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.ScalingLazyListState
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.ui.tooling.preview.WearPreviewSmallRound
import com.dashlane.design.iconography.IconTokens
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.wear.R
import com.dashlane.wear.ui.component.Emphasis
import com.dashlane.wear.ui.component.WearButton
import com.dashlane.wear.ui.component.WearIcon
import com.dashlane.wear.ui.component.WearText
import com.dashlane.wear.ui.screen.WearContentWrapper
import com.dashlane.wear.ui.screen.WearGenericLoadingScreen

@Composable
fun WearConfirmLogoutScreen(
    viewModel: WearLogoutViewModel,
    onCancel: () -> Unit,
    onLogoutComplete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(viewModel) {
        viewModel.stateFlow.sideEffect.collect { sideEffect ->
            when (sideEffect) {
                WearLogoutState.SideEffect.LogoutComplete -> {
                    onLogoutComplete()
                }
            }
        }
    }

    val uiState by viewModel.stateFlow.viewState.collectAsStateWithLifecycle()
    val scalingListState = rememberScalingLazyListState(initialCenterItemIndex = 1)

    WearContentWrapper(
        isTimeTextVisible = false,
        isLogoVisible = false,
        scrollableState = scalingListState,
    ) { contentPadding ->
        if (uiState.isLogoutInProgress) {
            WearGenericLoadingScreen(
                text = "Logging out",
            )
        } else {
            WearLogoutConfirmation(
                modifier = modifier,
                scalingListState = scalingListState,
                contentPadding = contentPadding,
                onConfirm = viewModel::onLogoutConfirmed,
                onCancel = onCancel,
            )
        }
    }
}

@Composable
fun WearLogoutConfirmation(
    scalingListState: ScalingLazyListState,
    contentPadding: PaddingValues,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ScalingLazyColumn(
        modifier = modifier.padding(horizontal = 16.dp),
        state = scalingListState,
        contentPadding = contentPadding,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item(key = "LogoutWarningIcon") {
            WearIcon(
                token = IconTokens.feedbackWarningFilled,
                contentDescription = "",
                tint = DashlaneTheme.colors.textInverseCatchy.value,
            )
        }

        item(key = "LogoutWarningBody") {
            WearText(
                textAlign = TextAlign.Center,
                maxLines = 10,
                text = stringResource(R.string.settings_logout_warning),
                style = DashlaneTheme.typography.bodyReducedRegular,
            )
        }

        item(key = "LogoutWarningCTAs") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(
                    space = 12.dp,
                    alignment = Alignment.CenterHorizontally,
                ),
            ) {
                WearButton(
                    token = IconTokens.actionCloseOutlined,
                    onClick = onCancel,
                    contentDescription = "",
                    emphasis = Emphasis.Low,
                )
                WearButton(
                    token = IconTokens.checkmarkOutlined,
                    onClick = { onConfirm() },
                    contentDescription = "",
                    emphasis = Emphasis.High,
                )
            }
        }
    }
}

@Composable
@WearPreviewSmallRound
private fun WearLogoutConfirmationPreview() {
    WearContentWrapper(isLogoVisible = true) { contentPadding ->
        val scalingListState = rememberScalingLazyListState(initialCenterItemIndex = 1)
        WearLogoutConfirmation(
            scalingListState = scalingListState,
            contentPadding = contentPadding,
            onCancel = {},
            onConfirm = {},
        )
    }
}