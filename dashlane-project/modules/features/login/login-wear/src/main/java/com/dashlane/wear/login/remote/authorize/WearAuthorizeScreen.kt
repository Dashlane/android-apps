package com.dashlane.wear.login.remote.authorize

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.wear.compose.ui.tooling.preview.WearPreviewSmallRound
import com.dashlane.design.iconography.IconTokens
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.wear.login.R
import com.dashlane.wear.login.common.ui.WearLoginErrorContent
import com.dashlane.wear.ui.DashlaneWearPreview
import com.dashlane.wear.ui.component.WearIcon
import com.dashlane.wear.ui.component.WearText
import com.dashlane.wear.ui.screen.WearContentWrapper
import com.dashlane.wear.ui.screen.WearGenericLoadingScreen

@Composable
fun WearAuthorizeScreen(
    viewModel: WearAuthorizeViewModel,
    onSuccess: () -> Unit,
    onRetry: () -> Unit,
    goToFreeUserPaywall: () -> Unit,
) {
    val uiState by viewModel.stateFlow.viewState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.login()
        viewModel.stateFlow.sideEffect.collect {
            when (it) {
                is WearAuthorizeState.SideEffect.OnSuccess -> onSuccess()
                is WearAuthorizeState.SideEffect.GoToFreeUserPaywall -> goToFreeUserPaywall()
            }
        }
    }

    WearContentWrapper {
        when (uiState) {
            WearAuthorizeState.View.Initial -> {
                WearGenericLoadingScreen()
            }
            WearAuthorizeState.View.Success -> {
                SuccessLoginContent()
            }
            is WearAuthorizeState.View.Error -> {
                WearLoginErrorContent(
                    errorText = stringResource(R.string.secret_transfer_error),
                    onButtonClick = onRetry,
                )
            }
        }
    }
}

@Composable
private fun SuccessLoginContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(
            space = 4.dp,
            alignment = Alignment.CenterVertically,
        ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        WearIcon(
            token = IconTokens.feedbackSuccessFilled,
            tint = DashlaneTheme.colors.containerExpressivePositiveCatchyIdle,
            contentDescription = "",
        )
        WearText(
            text = stringResource(R.string.authorize_login_success),
            style = DashlaneTheme.typography.bodyReducedRegular,
            textAlign = TextAlign.Center,
        )
    }
}

@WearPreviewSmallRound
@Composable
private fun SuccessLoginContentPreview() {
    DashlaneWearPreview {
        WearContentWrapper {
            SuccessLoginContent()
        }
    }
}