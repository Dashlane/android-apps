package com.dashlane.wear.login.paywall

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.ScalingLazyListState
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.ui.tooling.preview.WearPreviewSmallRound
import com.dashlane.design.iconography.IconTokens
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.devicediscovery.PairedDevice
import com.dashlane.wear.login.R
import com.dashlane.wear.login.common.ui.CheckYourPhoneScreenContent
import com.dashlane.wear.ui.DashlaneWearPreview
import com.dashlane.wear.ui.component.Emphasis
import com.dashlane.wear.ui.component.WearChip
import com.dashlane.wear.ui.component.WearIcon
import com.dashlane.wear.ui.component.WearText
import com.dashlane.wear.ui.screen.WearContentWrapper
import kotlin.time.Duration.Companion.seconds

private const val ANIMATION_DURATION = 300

@Composable
@Suppress("LongMethod")
fun FreeUserPaywallScreen(
    viewModel: FreeUserPaywallViewModel,
    onBack: () -> Unit,
) {
    LaunchedEffect(viewModel) {
        viewModel.onViewStarted()
        viewModel.stateFlow.sideEffect.collect { state ->
            when (state) {
                FreeUserPaywallState.SideEffect.GoBack -> onBack()
            }
        }
    }
    val state = viewModel.stateFlow.viewState.collectAsState()
    AnimatedContent(
        contentKey = { state.value.transitionKey },
        targetState = state.value,
        transitionSpec = {
            slideInHorizontally(
                animationSpec = tween(ANIMATION_DURATION),
                initialOffsetX = { fullWidth -> fullWidth },
            ) togetherWith
                slideOutHorizontally(
                    animationSpec = tween(ANIMATION_DURATION),
                    targetOffsetX = { fullWidth -> -fullWidth },
                )
        },
    ) { targetState ->
        when (targetState) {
            is FreeUserPaywallState.View.Default -> Unit
            is FreeUserPaywallState.View.Paywall -> {
                val scalingListState = rememberScalingLazyListState(
                    initialCenterItemIndex = 1,
                    initialCenterItemScrollOffset = 0,
                )
                WearContentWrapper(
                    scrollableState = scalingListState,
                    positionIndicator = {
                        PositionIndicator(scalingLazyListState = scalingListState)
                    },
                ) { contentPadding ->
                    FreeUserPaywallScreenContent(
                        contentPadding = contentPadding,
                        scalingLazyListState = scalingListState,
                        pairedDevice = targetState.devicesState,
                        onUpgradeClick = viewModel::onUpgrade,
                        onCancelClick = viewModel::onCancel,
                    )
                }
            }
            FreeUserPaywallState.View.CheckYourPhone -> {
                WearContentWrapper(
                    isLogoVisible = true,
                ) { _ ->
                    CheckYourPhoneScreenContent(
                        text = stringResource(R.string.wear_check_your_phone_upgrade),
                        waitingTime = 5.seconds,
                        onDelayComplete = viewModel::onCancel,
                    )
                }
            }
        }
    }
}

@Composable
fun FreeUserPaywallScreenContent(
    contentPadding: PaddingValues,
    scalingLazyListState: ScalingLazyListState,
    pairedDevice: PairedDevice?,
    onUpgradeClick: (PairedDevice) -> Unit,
    onCancelClick: () -> Unit,
) {
    ScalingLazyColumn(
        modifier = Modifier.padding(horizontal = 16.dp),
        state = scalingLazyListState,
        contentPadding = PaddingValues(top = contentPadding.calculateTopPadding()),
    ) {
        item(key = "warningIcon") {
            WearIcon(
                token = IconTokens.feedbackWarningFilled,
                tint = DashlaneTheme.colors.containerExpressiveDangerCatchyIdle,
                contentDescription = stringResource(R.string.error),
            )
        }
        item(key = "description") {
            WearText(
                modifier = Modifier.padding(top = 4.dp),
                textAlign = TextAlign.Center,
                text = stringResource(R.string.wear_free_user_paywall_body),
                style = DashlaneTheme.typography.bodyReducedRegular,
                maxLines = 5,
            )
        }
        
        pairedDevice?.let {
            item(key = "buttonUpgrade") {
                WearChip(
                    icon = {
                        WearIcon(
                            token = IconTokens.itemPhoneMobileOutlined,
                            contentDescription = "",
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    label = stringResource(R.string.wear_free_user_paywall_cta_upgrade),
                    labelMaxLines = 2,
                    onClick = { onUpgradeClick(it) },
                    emphasis = Emphasis.High,
                )
            }
        }
        item(key = "buttonCancel") {
            WearChip(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                label = stringResource(R.string.wear_free_user_paywall_cta_cancel),
                onClick = onCancelClick,
                emphasis = Emphasis.Medium,
            )
        }
    }
}

@WearPreviewSmallRound
@Composable
private fun FreeUserPaywallScreenContentWithoutUpgradePreview() {
    DashlaneWearPreview {
        FreeUserPaywallScreenContent(
            onUpgradeClick = {},
            onCancelClick = {},
            pairedDevice = null,
            contentPadding = PaddingValues(),
            scalingLazyListState = rememberScalingLazyListState(),
        )
    }
}

@WearPreviewSmallRound
@Composable
private fun FreeUserPaywallScreenContentWithUpgradePreview() {
    DashlaneWearPreview {
        FreeUserPaywallScreenContent(
            onUpgradeClick = {},
            onCancelClick = {},
            pairedDevice = PairedDevice(
                id = "id",
                name = "Pixel_3a",
                isNearby = true,
                hasDashlane = true,
            ),
            contentPadding = PaddingValues(),
            scalingLazyListState = rememberScalingLazyListState(),
        )
    }
}