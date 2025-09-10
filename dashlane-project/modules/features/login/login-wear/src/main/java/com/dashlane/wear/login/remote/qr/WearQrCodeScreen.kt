package com.dashlane.wear.login.remote.qr

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.wear.login.R
import com.dashlane.wear.login.common.ui.WearLoginErrorContent
import com.dashlane.wear.login.remote.qr.QrCodeError.QrCodeGenerationError
import com.dashlane.wear.login.remote.qr.WearQrCodeState.View.Error
import com.dashlane.wear.login.remote.qr.WearQrCodeState.View.Loading
import com.dashlane.wear.login.remote.qr.WearQrCodeState.View.QrCode
import com.dashlane.wear.login.remote.qr.ui.WearQrCode
import com.dashlane.wear.login.remote.qr.ui.WearQrConfirmEmail
import com.dashlane.wear.login.remote.qr.ui.getQRCodeSizeForConfiguration
import com.dashlane.wear.ui.backgroundDefaultWear
import com.dashlane.wear.ui.screen.WearContentWrapper
import com.dashlane.wear.ui.screen.WearGenericLoadingScreen

@Composable
fun WearQrCodeScreen(
    viewModel: WearQrCodeViewModel,
    goToAuthorize: () -> Unit,
    goToOtpChallenge: () -> Unit,
) {
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val qrCodeColor = DashlaneTheme.colors.backgroundDefaultWear.toArgb()
    val generateQrCode = {
        viewModel.generateQrCode(
            qrCodeSize = getQRCodeSizeForConfiguration(density, configuration),
            qrCodeColor = qrCodeColor,
        )
    }

    LaunchedEffect(viewModel) {
        generateQrCode()
        viewModel.stateFlow.sideEffect.collect { state ->
            when (state) {
                WearQrCodeState.SideEffect.GoToAuthorization -> goToAuthorize()
                is WearQrCodeState.SideEffect.GoToOtpChallenge -> goToOtpChallenge()
            }
        }
    }

    val uiState by viewModel.stateFlow.viewState.collectAsStateWithLifecycle()
    WearContentWrapper(
        isLogoVisible = uiState.isLogoVisible,
    ) { contentPadding ->
        when (val state = uiState) {
            is Loading -> WearGenericLoadingScreen(
                text = stringResource(R.string.secret_transfer_loading_qr_code),
            )
            is QrCode -> WearQrCode(
                qrCode = state.qrCodeBitmap,
            )
            is WearQrCodeState.View.EmailConfirmation ->
                WearQrConfirmEmail(
                    modifier = Modifier.padding(
                        top = contentPadding.calculateTopPadding(),
                        start = 16.dp,
                        end = 16.dp,
                        bottom = 16.dp,
                    ),
                    email = state.secretTransferPayload.login,
                    onCancel = { generateQrCode() },
                    onAccept = { viewModel.emailConfirmed(state.secretTransferPayload) },
                )
            is Error -> WearLoginErrorContent(
                Modifier.padding(
                    top = 24.dp,
                    start = 16.dp,
                    end = 16.dp,
                    bottom = 16.dp,
                ),
                errorText = state.error.toErrorMessage(),
                onButtonClick = generateQrCode,
            )
        }
    }
}

@Composable
private fun QrCodeError?.toErrorMessage(): String = if (this is QrCodeGenerationError && this.isDeviceOffline) {
    stringResource(R.string.offline)
} else {
    stringResource(R.string.secret_transfer_error)
}