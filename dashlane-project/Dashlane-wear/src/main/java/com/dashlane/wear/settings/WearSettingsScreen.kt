package com.dashlane.wear.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.ui.tooling.preview.WearPreviewSmallRound
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.wear.BuildConfig
import com.dashlane.wear.R
import com.dashlane.wear.ui.DashlaneWearPreview
import com.dashlane.wear.ui.component.Emphasis
import com.dashlane.wear.ui.component.WearChip
import com.dashlane.wear.ui.component.WearText
import com.dashlane.wear.ui.component.WearTitleCard
import com.dashlane.wear.ui.screen.WearContentWrapper

@Composable
fun WearSettingsScreen(
    viewModel: WearSettingsViewModel,
    askLogoutConfirmation: () -> Unit,
) {
    LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val lifecycleState by lifecycleOwner.lifecycle.currentStateFlow.collectAsStateWithLifecycle()

    LaunchedEffect(lifecycleState) {
        
        when (lifecycleState) {
            Lifecycle.State.RESUMED -> viewModel.reloadSettings()
            else -> Unit
        }
    }
    LaunchedEffect(viewModel) {
        viewModel.reloadSettings()
        viewModel.stateFlow.sideEffect.collect { sideEffect ->
            when (sideEffect) {
                WearSettingsState.SideEffect.AskLogoutConfirmation -> askLogoutConfirmation()
                WearSettingsState.SideEffect.OpenMobilePlans -> {
                    
                }
            }
        }
    }

    val uiState by viewModel.stateFlow.viewState.collectAsStateWithLifecycle()
    WearSettingsScreenContent(
        uiState = uiState,
        onPlanClick = viewModel::openPlan,
        onLogoutClick = viewModel::askLogoutConfirmation,
        onSetCredentialProvider = viewModel::setCredentialProvider,
    )
}

@SuppressWarnings("LongMethod")
@Composable
fun WearSettingsScreenContent(
    modifier: Modifier = Modifier,
    uiState: WearSettingsState.View,
    onPlanClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onSetCredentialProvider: () -> Unit,
) {
    val scalingListState = rememberScalingLazyListState(initialCenterItemIndex = 1)

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
            contentPadding = PaddingValues(top = contentPadding.calculateTopPadding()),
        ) {
            item(key = "SettingsHeader") {
                WearText(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    text = stringResource(R.string.vault_settings_item),
                    style = DashlaneTheme.typography.bodyReducedRegular.copy(fontWeight = FontWeight.Bold),
                    color = DashlaneTheme.colors.textInverseQuiet,
                    textAlign = TextAlign.Center,
                )
            }
            item(key = "UserLogin") {
                WearTitleCard(
                    enabled = false,
                    modifier = Modifier.fillMaxWidth(),
                    title = stringResource(R.string.settings_login_email),
                    content = uiState.login,
                    emphasis = Emphasis.Low,
                )
            }
            item(key = "PlanDetails") {
                WearChip(
                    modifier = Modifier.fillMaxWidth(),
                    label = stringResource(R.string.settings_plan_name),
                    secondaryLabel = uiState.plan,
                    emphasis = Emphasis.Low,
                    onClick = onPlanClick,
                    contentMaxLines = 2,
                )
            }
            
            if (uiState.showCredentialManagerAction) {
                item(key = "SetAsCredentialProvider") {
                    WearChip(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        label = stringResource(R.string.settings_set_credential_provider_button),
                        emphasis = Emphasis.Medium,
                        onClick = onSetCredentialProvider,
                        labelMaxLines = 2,
                    )
                }
            }
            item(key = "LogOut") {
                WearChip(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    label = stringResource(R.string.settings_logout_button),
                    emphasis = Emphasis.Danger,
                    onClick = onLogoutClick,
                )
            }
            item(key = "AppVersion") {
                Column(
                    modifier = Modifier.padding(top = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    WearText(
                        text = stringResource(R.string.settings_app_version),
                        style = DashlaneTheme.typography.bodyReducedRegular.copy(fontWeight = FontWeight.Bold),
                        color = DashlaneTheme.colors.textInverseQuiet,
                    )
                    WearText(
                        text = BuildConfig.VERSION_NAME,
                        style = DashlaneTheme.typography.bodyReducedRegular,
                        color = DashlaneTheme.colors.textInverseQuiet,
                    )
                }
            }
        }
    }
}

@Composable
@WearPreviewSmallRound
fun WearSettingsScreenContentPreview() {
    DashlaneWearPreview {
        WearSettingsScreenContent(
            uiState = WearSettingsState.View(
                login = "randomemail@provider.com",
                plan = "Business",
                showCredentialManagerAction = true,
            ),
            onLogoutClick = {},
            onPlanClick = {},
            onSetCredentialProvider = {},
        )
    }
}