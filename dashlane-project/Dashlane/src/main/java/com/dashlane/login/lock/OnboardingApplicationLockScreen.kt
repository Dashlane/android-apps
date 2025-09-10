package com.dashlane.login.lock

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dashlane.R
import com.dashlane.design.component.ButtonLayout
import com.dashlane.design.component.ButtonMedium
import com.dashlane.design.component.IndeterminateLoader
import com.dashlane.design.iconography.IconTokens
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.color.Intensity
import com.dashlane.design.theme.tooling.BooleanProvider
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.login.lock.OnboardingApplicationLockEvent.ViewResumed
import com.dashlane.login.lock.OnboardingApplicationLockState.SideEffect
import com.dashlane.ui.common.compose.components.basescreen.BoardingContent
import com.dashlane.ui.common.compose.components.basescreen.BoardingScreenDefaults
import com.dashlane.ui.common.compose.components.basescreen.scaffoldBackgroundAndInsets

@Composable
internal fun OnboardingApplicationLockScreen(
    viewModel: OnboardingApplicationLockViewModel,
    goToNextStep: (Intent) -> Unit,
) {
    val biometricActivationLauncher = rememberLauncherForActivityResult(BiometricActivationResult()) { enabled ->
        if (enabled) {
            viewModel.sendEvent(OnboardingApplicationLockEvent.BiometricSetupDone)
        }
    }

    val pinCodeSetterLauncher = rememberLauncherForActivityResult(PinCodeSetterResult()) { enabled ->
        if (enabled) {
            viewModel.sendEvent(OnboardingApplicationLockEvent.PinCodeSetupDone)
        }
    }

    val lifecycleOwner = rememberUpdatedState(newValue = LocalLifecycleOwner.current)
    DisposableEffect(lifecycleOwner.value) {
        val lifecycle = lifecycleOwner.value.lifecycle
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> viewModel.sendEvent(ViewResumed)
                else -> Unit
            }
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(viewModel) {
        viewModel.stateFlow.sideEffect.collect { sideEffect ->
            when (sideEffect) {
                SideEffect.GoToBiometricActivation -> biometricActivationLauncher.launch(Unit)
                is SideEffect.GoToNext -> goToNextStep(sideEffect.nextIntent)
                SideEffect.GoToPinCodeSetter -> pinCodeSetterLauncher.launch(Unit)
            }
        }
    }

    val uiState by viewModel.stateFlow.viewState.collectAsStateWithLifecycle()
    when (uiState) {
        is OnboardingApplicationLockState.View.Initial,
        is OnboardingApplicationLockState.View.Loading,
        -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                IndeterminateLoader()
            }
        }

        is OnboardingApplicationLockState.View.SuggestLockSetup -> {
            SuggestLockSetupContent(
                hasBiometricHardware = uiState.data.hasBiometricHardware!!,
                onSkipClick = { viewModel.sendEvent(OnboardingApplicationLockEvent.SkipClicked) },
                onBiometricActivationClick = { viewModel.sendEvent(OnboardingApplicationLockEvent.UseBiometricClicked) },
                onPinCodeSetupClick = { viewModel.sendEvent(OnboardingApplicationLockEvent.UsePinCodeClicked) },
            )
        }
    }
}

@Composable
private fun SuggestLockSetupContent(
    hasBiometricHardware: Boolean,
    onSkipClick: () -> Unit,
    onBiometricActivationClick: () -> Unit,
    onPinCodeSetupClick: () -> Unit,
) {
    if (hasBiometricHardware) {
        BoardingScreenWithSkip(
            decoration = BoardingScreenDefaults.decoration(iconToken = IconTokens.lockOutlined),
            title = stringResource(R.string.onboarding_application_lock_title_default),
            description = stringResource(R.string.onboarding_application_lock_description_default),
            actions = BoardingScreenDefaults.Actions(
                primaryButtonText = stringResource(R.string.onboarding_application_lock_cta_positive_default),
                onPrimaryButtonClick = onBiometricActivationClick,
                secondaryButtonText = stringResource(R.string.onboarding_application_lock_cta_negative_default),
                onSecondaryButtonClick = onPinCodeSetupClick,
            ),
            onSkipClick = onSkipClick,
        )
    } else {
        BoardingScreenWithSkip(
            decoration = BoardingScreenDefaults.decoration(iconToken = IconTokens.lockOutlined),
            title = stringResource(R.string.onboarding_application_lock_title_pin),
            description = stringResource(R.string.onboarding_application_lock_description_pin),
            actions = BoardingScreenDefaults.Actions(
                primaryButtonText = stringResource(R.string.onboarding_application_lock_cta_positive_pin),
                onPrimaryButtonClick = onPinCodeSetupClick,
                secondaryButtonText = null,
                onSecondaryButtonClick = null,
            ),
            onSkipClick = onSkipClick,
        )
    }
}

@Composable
private fun BoardingScreenWithSkip(
    decoration: @Composable ColumnScope.() -> Unit,
    title: String,
    description: String,
    actions: BoardingScreenDefaults.Actions,
    onSkipClick: () -> Unit,
) {
    Scaffold(
        modifier = Modifier.scaffoldBackgroundAndInsets(),
        containerColor = DashlaneTheme.colors.backgroundAlternate,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.End,
            ) {
                ButtonMedium(
                    layout = ButtonLayout.TextOnly(stringResource(R.string.onboarding_application_lock_cta_skip_default)),
                    onClick = onSkipClick,
                    intensity = Intensity.Supershy,
                )
            }
        },
    ) { scaffoldContentPadding ->
        BoardingContent(
            modifier = Modifier.padding(scaffoldContentPadding),
            decoration = decoration,
            title = title,
            description = description,
            actions = actions,
        )
    }
}

@Preview
@Composable
private fun SuggestLockSetupContentPreview(
    @PreviewParameter(BooleanProvider::class) hasBiometric: Boolean,
) {
    DashlanePreview {
        SuggestLockSetupContent(
            hasBiometricHardware = hasBiometric,
            onSkipClick = {},
            onBiometricActivationClick = {},
            onPinCodeSetupClick = {},
        )
    }
}