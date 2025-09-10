package com.dashlane.premium.current.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dashlane.design.component.ButtonMediumBar
import com.dashlane.design.component.IndeterminateLoader
import com.dashlane.design.component.InfoboxMedium
import com.dashlane.design.component.Text
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.color.Mood
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.premium.current.CurrentPlanState
import com.dashlane.premium.current.CurrentPlanViewModel
import com.dashlane.premium.current.MoreInfo
import com.dashlane.premium.current.model.CurrentPlan
import com.dashlane.premium.current.ui.CurrentPlanDefaults.constraintContent
import com.dashlane.premium.current.ui.CurrentPlanDefaults.horizontalPadding
import com.dashlane.premium.ui.R
import com.dashlane.ui.common.compose.components.basescreen.scaffoldBackgroundAndInsets
import com.dashlane.ui.model.TextResource
import com.dashlane.ui.model.getText

@Composable
internal fun CurrentPlanScreen(
    viewModel: CurrentPlanViewModel,
    onCloseWithoutAction: () -> Unit,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val lifecycleState by lifecycleOwner.lifecycle.currentStateFlow.collectAsStateWithLifecycle()
    LaunchedEffect(lifecycleState) {
        when (lifecycleState) {
            Lifecycle.State.RESUMED -> viewModel.refresh()
            else -> Unit
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.stateFlow.sideEffect.collect { sideEffect ->
            when (sideEffect) {
                is CurrentPlanState.SideEffect.CloseWithoutAction -> onCloseWithoutAction()
            }
        }
    }

    Scaffold(
        modifier = Modifier.scaffoldBackgroundAndInsets(),
        containerColor = DashlaneTheme.colors.backgroundAlternate,
    ) { contentPadding ->
        val uiState by viewModel.stateFlow.viewState.collectAsStateWithLifecycle()
        when (val viewState = uiState) {
            CurrentPlanState.View.Initial -> {
                
            }

            CurrentPlanState.View.Loading -> {
                LoadingCurrentPlan(
                    modifier = Modifier.padding(contentPadding),
                )
            }

            is CurrentPlanState.View.Loaded -> {
                CurrentPlanContent(
                    modifier = Modifier.padding(contentPadding),
                    currentPlan = viewState.currentPlan,
                    onItemClick = viewModel::onBenefitItemClicked,
                    onActionClick = viewModel::onActionClicked,
                )

                when (viewState.moreInfo) {
                    MoreInfo.Hidden -> Unit 
                    is MoreInfo.Displaying -> {
                        when (viewState.moreInfo.moreInfoType) {
                            CurrentPlan.Benefit.MoreInfoType.DARK_WEB_MONITORING -> {
                                DarkWebMonitoringBottomSheet(
                                    onDismissRequest = viewModel::onDarkWebMonitoringInfoDismiss,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingCurrentPlan(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        IndeterminateLoader()
    }
}

@Suppress("LongMethod")
@Composable
private fun CurrentPlanContent(
    currentPlan: CurrentPlan,
    onItemClick: (CurrentPlan.Benefit.MoreInfoType) -> Unit,
    onActionClick: (CurrentPlan.Action.Type, List<CurrentPlan.Action.Type>) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .then(modifier),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        LazyColumn(
            modifier = Modifier
                .weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            item {
                CurrentPlanHeader()
            }
            item {
                Text(
                    modifier = Modifier
                        .constraintContent()
                        .semantics { heading() }
                        .padding(horizontal = horizontalPadding),
                    text = currentPlan.title.getText(),
                    style = DashlaneTheme.typography.titleSectionLarge,
                    color = DashlaneTheme.colors.textNeutralCatchy,
                )
            }
            if (currentPlan.benefits.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
            items(currentPlan.benefits) { benefit ->
                BenefitItem(
                    modifier = Modifier.constraintContent(),
                    text = benefit.textResource.getText(),
                    moreInfoType = benefit.moreInfoType,
                    onClick = onItemClick,
                )
            }
            if (currentPlan.suggestion != null) {
                item {
                    val suggestion = currentPlan.suggestion
                    InfoboxMedium(
                        modifier = Modifier
                            .constraintContent()
                            .padding(horizontal = horizontalPadding, vertical = 16.dp),
                        title = suggestion.title?.getText() ?: suggestion.text.getText(),
                        description = suggestion.text.getText().takeIf { suggestion.title != null },
                        mood = Mood.Brand,
                    )
                }
            }
        }
        val recommendedActions = listOfNotNull(
            currentPlan.primaryAction.type,
            currentPlan.secondaryAction?.type,
        )
        ButtonMediumBar(
            modifier = Modifier
                .constraintContent()
                .padding(horizontal = horizontalPadding, vertical = 4.dp),
            primaryText = stringResource(currentPlan.primaryAction.label),
            onPrimaryButtonClick = { onActionClick(currentPlan.primaryAction.type, recommendedActions) },
            secondaryText = currentPlan.secondaryAction?.let { stringResource(it.label) },
            onSecondaryButtonClick = { currentPlan.secondaryAction?.let { onActionClick(it.type, recommendedActions) } },
        )
    }
}

internal object CurrentPlanDefaults {
    val horizontalPadding = 16.dp

    fun Modifier.constraintContent() = this
        .widthIn(max = 480.dp)
        .fillMaxWidth()
}

@Preview
@Composable
private fun LoadingCurrentPlanPreview() {
    DashlanePreview {
        LoadingCurrentPlan()
    }
}

@Preview
@Composable
private fun CurrentPlanContentPreview() {
    DashlanePreview {
        CurrentPlanContent(
            currentPlan = CurrentPlan(
                title = TextResource.StringText(R.string.current_plan_title_trial),
                benefits = listOf(
                    CurrentPlan.Benefit(
                        textResource = TextResource.StringText(stringRes = R.string.current_benefit_passwords_unlimited),
                        moreInfoType = null,
                    ),
                    CurrentPlan.Benefit(
                        textResource = TextResource.StringText(stringRes = R.string.current_benefit_devices_sync_unlimited),
                        moreInfoType = null,
                    ),
                    CurrentPlan.Benefit(
                        textResource = TextResource.StringText(stringRes = R.string.current_benefit_secure_notes),
                        moreInfoType = null,
                    ),
                    CurrentPlan.Benefit(
                        textResource = TextResource.StringText(stringRes = R.string.current_benefit_dark_web_monitoring),
                        moreInfoType = CurrentPlan.Benefit.MoreInfoType.DARK_WEB_MONITORING,
                    ),
                    CurrentPlan.Benefit(
                        textResource = TextResource.StringText(stringRes = R.string.current_benefit_phishing_alert),
                        moreInfoType = null,
                    ),
                    CurrentPlan.Benefit(
                        textResource = TextResource.StringText(stringRes = R.string.current_benefit_vpn),
                        moreInfoType = null,
                    ),
                    CurrentPlan.Benefit(
                        textResource = TextResource.StringText(stringRes = R.string.current_benefit_secure_file_storage),
                        moreInfoType = null,
                    ),
                    CurrentPlan.Benefit(
                        textResource = TextResource.StringText(stringRes = R.string.current_benefit_sharing),
                        moreInfoType = null,
                    ),
                ),
                suggestion = CurrentPlan.Suggestion(
                    title = TextResource.StringText(R.string.current_plan_suggestion_trial_title),
                    text = TextResource.StringText(R.string.current_plan_suggestion_trial_text),
                ),
                primaryAction = CurrentPlan.Action(
                    type = CurrentPlan.Action.Type.ALL_PLANS,
                    R.string.current_plan_cta_all_plans,
                ),
                secondaryAction = CurrentPlan.Action(
                    type = CurrentPlan.Action.Type.CLOSE,
                    R.string.current_plan_cta_close,
                ),
            ),
            onItemClick = {},
            onActionClick = { _, _ -> },
        )
    }
}