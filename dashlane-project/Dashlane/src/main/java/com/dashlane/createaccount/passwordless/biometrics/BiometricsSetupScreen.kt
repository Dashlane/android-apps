package com.dashlane.createaccount.passwordless.biometrics

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.dashlane.ui.widgets.compose.LoadingScreen

@Composable
fun BiometricsSetupScreen(
    viewModel: BiometricsSetupViewModel,
    onSkip: () -> Unit,
    onBiometricsEnabled: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        when (uiState) {
            BiometricSetupState.HardwareDisabled -> onSkip()
            else -> Unit
        }
    }

    when (uiState) {
        BiometricSetupState.HardwareEnabled -> BiometricsSetupContent(onSkip = onSkip, onNext = onBiometricsEnabled)
        BiometricSetupState.Loading -> LoadingScreen(title = "")
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
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .weight(1f)
                .padding(top = 24.dp, end = 24.dp, bottom = 24.dp, start = 24.dp)
        ) {
            Image(
                modifier = Modifier
                    .size(96.dp),
                painter = painterResource(id = R.drawable.ic_biometrics),
                colorFilter = ColorFilter.tint(DashlaneTheme.colors.containerExpressiveBrandCatchyIdle),
                contentDescription = null
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
        }
        ButtonMediumBar(
            modifier = modifier
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
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
fun BiometricPlaceHolderScreenPreview() {
    DashlanePreview {
        BiometricsSetupContent(
            modifier = Modifier,
            onSkip = {},
            onNext = {}
        )
    }
}