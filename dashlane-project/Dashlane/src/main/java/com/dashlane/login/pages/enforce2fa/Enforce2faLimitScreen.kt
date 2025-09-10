package com.dashlane.login.pages.enforce2fa

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.dashlane.R
import com.dashlane.design.iconography.IconTokens
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.ui.common.compose.components.basescreen.BoardingScreen
import com.dashlane.ui.common.compose.components.basescreen.BoardingScreenDefaults

@Composable
internal fun Enforce2faLimitScreen(
    viewModel: Enforce2faLimitViewModel,
    goToLogin: () -> Unit,
) {
    BackHandler(
        enabled = true,
        onBack = {
            
        },
    )

    val lifecycleOwner = rememberUpdatedState(newValue = LocalLifecycleOwner.current)
    DisposableEffect(lifecycleOwner.value) {
        val lifecycle = lifecycleOwner.value.lifecycle
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> viewModel.sendEvent(Enforce2faLimitEvent.ViewResumed)
                Lifecycle.Event.ON_PAUSE -> viewModel.sendEvent(Enforce2faLimitEvent.ViewPaused)
                else -> Unit
            }
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(viewModel) {
        viewModel.stateFlow.sideEffect.collect { sideEffect ->
            when (sideEffect) {
                Enforce2faLimitState.SideEffect.GoToLogin -> goToLogin()
            }
        }
    }

    Enforce2faLimitScreen(
        onLogoutClick = { viewModel.sendEvent(Enforce2faLimitEvent.LogoutClick) },
    )
}

@Composable
private fun Enforce2faLimitScreen(
    onLogoutClick: () -> Unit,
) {
    BoardingScreen(
        decoration = BoardingScreenDefaults.decoration(iconToken = IconTokens.featureAuthenticatorOutlined),
        title = stringResource(id = R.string.login_dialog_enforce_twofa_limit_title),
        description = stringResource(R.string.login_dialog_enforce_twofa_limit_description_new),
        actions = BoardingScreenDefaults.Actions(
            primaryButtonText = stringResource(R.string.login_dialog_enforce_twofa_negative_button),
            onPrimaryButtonClick = onLogoutClick,
            secondaryButtonText = null,
            onSecondaryButtonClick = null,
        ),
    )
}

@Preview
@Composable
private fun Enforce2faLimitScreenPreview() {
    DashlanePreview {
        Enforce2faLimitScreen(
            onLogoutClick = {},
        )
    }
}