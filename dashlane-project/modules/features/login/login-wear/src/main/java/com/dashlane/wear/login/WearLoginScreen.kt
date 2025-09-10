package com.dashlane.wear.login

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.wear.compose.ui.tooling.preview.WearPreviewSmallRound
import com.dashlane.wear.login.common.ui.WearLoginErrorContent
import com.dashlane.wear.ui.DashlaneWearTheme
import com.dashlane.wear.ui.screen.WearContentWrapper
import com.dashlane.wear.ui.screen.WearGenericLoadingScreen

@Composable
fun WearLoginScreen(
    viewModel: WearLoginViewModel,
    goToWelcome: () -> Unit,
    askSecondFactor: () -> Unit,
    onSessionRestored: (Boolean) -> Unit,
) {
    LaunchedEffect(viewModel) {
        viewModel.viewStarted()
        viewModel.stateFlow.sideEffect.collect { state ->
            when (state) {
                LoginState.SideEffect.GoWelcome -> goToWelcome()
                LoginState.SideEffect.GoSecondFactor -> askSecondFactor()
                is LoginState.SideEffect.SessionRestored -> onSessionRestored(state.canSetupCredentialManager)
            }
        }
    }
    val state = viewModel.stateFlow.viewState.collectAsState()
    WearLoginScreenContent(
        state = state.value,
        onRetry = { viewModel.viewStarted() },
    )
}

@Composable
fun WearLoginScreenContent(state: LoginState.View, onRetry: () -> Unit) {
    WearContentWrapper { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
        ) {
            if (state.error == null) {
                WearGenericLoadingScreen()
            } else {
                val error = state.error
                val errorString = when (error) {
                    LoginState.Error.SessionRestorationError -> R.string.generic_error_message
                }
                WearLoginErrorContent(
                    errorText = stringResource(errorString),
                    onButtonClick = onRetry,
                )
            }
        }
    }
}

@WearPreviewSmallRound
@Composable
private fun WearLoginScreenDefaultPreview() {
    DashlaneWearTheme {
        WearLoginScreenContent(
            state = LoginState.View(),
            onRetry = {},
        )
    }
}

@WearPreviewSmallRound
@Composable
private fun WearLoginScreenErrorPreview() {
    DashlaneWearTheme {
        WearLoginScreenContent(
            state = LoginState.View(error = LoginState.Error.SessionRestorationError),
            onRetry = {},
        )
    }
}