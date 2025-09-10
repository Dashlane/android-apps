package com.dashlane.wear.login.common.secondfactor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.ui.tooling.preview.WearPreviewSmallRound
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.wear.login.R
import com.dashlane.wear.ui.DashlaneWearPreview
import com.dashlane.wear.ui.component.ObfuscatedOtpField
import com.dashlane.wear.ui.component.PinKeyboard
import com.dashlane.wear.ui.component.WearText
import com.dashlane.wear.ui.screen.WearContentWrapper
import com.dashlane.wear.ui.screen.WearGenericLoadingScreen

@Composable
fun WearSecondFactorInputScreen(
    viewModel: WearSecondFactorViewModel,
    onSuccess: () -> Unit,
) {
    val uiState = viewModel.stateFlow.viewState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.viewStarted()

        viewModel.stateFlow.sideEffect.collect { state ->
            when (state) {
                is OtpState.SideEffect.Success -> {
                    onSuccess()
                }
            }
        }
    }

    WearTotpInputContent(
        code = uiState.value.token,
        isLoading = uiState.value.isLoading,
        hasError = uiState.value.error != null,
        onInput = viewModel::addDigit,
        onDelete = viewModel::deleteLastDigit,
        onValidate = viewModel::validateToken,
    )
}

@Composable
fun WearTotpInputContent(
    code: String,
    isLoading: Boolean,
    hasError: Boolean,
    onInput: (Char) -> Unit,
    onDelete: () -> Unit,
    onValidate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    WearContentWrapper { contentPadding ->
        if (isLoading) {
            WearGenericLoadingScreen()
        }
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(contentPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(
                space = 4.dp,
                alignment = Alignment.CenterVertically,
            ),
        ) {
            WearText(
                text = if (hasError) {
                    stringResource(R.string.totp_token_incorrect)
                } else {
                    stringResource(R.string.totp_token_title)
                },
                textAlign = TextAlign.Center,
                style = DashlaneTheme.typography.componentButtonStandard,
            )
            ObfuscatedOtpField(
                code = code,
            )
            PinKeyboard(
                onInput = onInput,
                onDelete = onDelete,
                onValidate = onValidate,
            )
        }
    }
}

@WearPreviewSmallRound
@Composable
private fun WearOtpInputScreenPreview() {
    DashlaneWearPreview {
        WearTotpInputContent(
            code = "1234",
            isLoading = false,
            hasError = false,
            onInput = {},
            onDelete = {},
            onValidate = {},
        )
    }
}

@WearPreviewSmallRound
@Composable
private fun WearOtpInputScreenLoadingPreview() {
    DashlaneWearPreview {
        WearTotpInputContent(
            code = "1234",
            isLoading = true,
            hasError = false,
            onInput = {},
            onDelete = {},
            onValidate = {},
        )
    }
}