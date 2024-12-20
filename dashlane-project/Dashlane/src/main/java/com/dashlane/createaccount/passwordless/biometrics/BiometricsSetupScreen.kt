package com.dashlane.createaccount.passwordless.biometrics

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dashlane.R
import com.dashlane.design.component.ButtonMediumBar
import com.dashlane.design.component.Text
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.ui.common.compose.components.LoadingScreen

@Composable
fun BiometricsSetupScreen(
    modifier: Modifier = Modifier,
    viewModel: BiometricsSetupViewModel,
    onSkip: () -> Unit,
    onBiometricsDisabled: () -> Unit,
    onBiometricsEnabled: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        when (uiState) {
            BiometricSetupState.HardwareDisabled -> onBiometricsDisabled()
            else -> Unit
        }
    }

    when (uiState) {
        BiometricSetupState.HardwareEnabled -> BiometricsSetupContent(modifier = modifier, onSkip = onSkip, onNext = onBiometricsEnabled)
        BiometricSetupState.Loading -> LoadingScreen(modifier = modifier, title = "")
        else -> Unit
    }
}

@Composable
fun BiometricsSetupContent(
    modifier: Modifier = Modifier,
    onSkip: () -> Unit,
    onNext: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 18.dp, top = 24.dp, start = 24.dp, end = 24.dp)
    ) {
        Image(
            modifier = Modifier
                .padding(top = 48.dp)
                .size(96.dp),
            painter = painterResource(id = R.drawable.ic_biometrics),
            colorFilter = ColorFilter.tint(DashlaneTheme.colors.containerExpressiveBrandCatchyIdle),
            contentDescription = stringResource(id = R.string.and_accessibility_content_desc_fingerprint_logo)
        )
        Text(
            modifier = Modifier.padding(top = 32.dp),
            text = stringResource(R.string.passwordless_biometrics_setup_title),
            style = DashlaneTheme.typography.titleSectionLarge
        )
        Text(
            modifier = Modifier.padding(top = 16.dp),
            text = stringResource(R.string.passwordless_biometrics_setup_description),
            style = DashlaneTheme.typography.bodyStandardRegular
        )
        Spacer(modifier = Modifier.weight(1f))
        ButtonMediumBar(
            modifier = Modifier
                .align(Alignment.End),
            primaryText = stringResource(id = R.string.passwordless_biometrics_positive_button),
            secondaryText = stringResource(id = R.string.passwordless_biometrics_neutral_button),
            onPrimaryButtonClick = onNext,
            onSecondaryButtonClick = onSkip
        )
    }
}

@Preview
@Composable
private fun BiometricPlaceHolderScreenPreview() {
    DashlanePreview {
        BiometricsSetupContent(
            modifier = Modifier,
            onSkip = {},
            onNext = {}
        )
    }
}