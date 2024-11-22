package com.dashlane.secrettransfer.qrcode

import android.content.res.Configuration
import android.graphics.Bitmap
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dashlane.authentication.RegisteredUserDevice
import com.dashlane.design.component.ButtonLayout
import com.dashlane.design.component.ButtonMedium
import com.dashlane.design.component.ButtonMediumBar
import com.dashlane.design.component.DashlaneLogoLockup
import com.dashlane.design.component.IndeterminateLoader
import com.dashlane.design.component.Text
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.color.Intensity
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.secrettransfer.domain.SecretTransferPayload
import com.dashlane.secrettransferqr.R
import com.dashlane.ui.common.compose.components.GenericErrorContent

@OptIn(ExperimentalMaterial3Api::class)
@Suppress("LongMethod")
@Composable
fun QrCodeScreen(
    modifier: Modifier = Modifier,
    viewModel: QrCodeViewModel,
    email: String? = null,
    onQrScanned: (SecretTransferPayload) -> Unit,
    onGoToARK: (RegisteredUserDevice.Local) -> Unit,
    onGoToUniversalD2D: (String) -> Unit,
    onCancelled: () -> Unit
) {
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val dashlaneColors = DashlaneTheme.colors

    LaunchedEffect(viewModel) {
        viewModel.viewStarted(
            email = email,
            qrCodeSize = getQrCodeSize(density, configuration),
            qrCodeColor = dashlaneColors.textNeutralCatchy.value.toArgb()
        )
        viewModel.stateFlow.sideEffect.collect { state ->
            when (state) {
                is QrCodeState.SideEffect.GoToConfirmEmail -> onQrScanned(state.secretTransferPayload)
                is QrCodeState.SideEffect.Cancelled -> onCancelled()
                is QrCodeState.SideEffect.GoToARK -> onGoToARK(state.registeredUserDevice)
                is QrCodeState.SideEffect.GoToUniversalD2D -> onGoToUniversalD2D(state.email)
            }
        }
    }

    val uiState by viewModel.stateFlow.viewState.collectAsStateWithLifecycle()
    val error = uiState.error
    if (error != null) {
        GenericErrorContent(
            title = stringResource(id = R.string.login_secret_transfer_data_error_title),
            message = stringResource(id = R.string.login_secret_transfer_data_error_message),
            textPrimary = stringResource(id = R.string.login_secret_transfer_error_button_retry),
            textSecondary = stringResource(id = R.string.login_secret_transfer_error_button_cancel),
            onClickPrimary = {
                viewModel.retry(
                    error = error,
                    qrCodeSize = with(density) { configuration.screenWidthDp.dp.roundToPx() },
                    qrCodeColor = dashlaneColors.textNeutralCatchy.value.toArgb()
                )
            },
            onClickSecondary = { viewModel.cancelOnError(error) }
        )
    } else {
        QrCodeContent(
            modifier = modifier,
            qrCode = uiState.qrCodeBitmap,
            isLoading = uiState.isLoading,
            isHelpEnabled = uiState.email != null,
            onOtherMethodClicked = viewModel::helpClicked
        )
    }

    if (uiState.bottomSheetVisible) {
        val sheetState = rememberModalBottomSheetState(
            confirmValueChange = { sheetValue ->
                if (sheetValue == SheetValue.Hidden) viewModel.bottomSheetDismissed()
                true
            }
        )

        ModalBottomSheet(
            containerColor = DashlaneTheme.colors.backgroundDefault,
            onDismissRequest = viewModel::bottomSheetDismissed,
            sheetState = sheetState
        ) {
            QRCodeBottomSheetContent(
                onPrimaryButtonClick = viewModel::universalD2DClicked,
                onSecondaryButtonClick = viewModel::arkClicked
            )
        }
    }
}

private fun getQrCodeSize(density: Density, configuration: Configuration) =
    with(density) {
        
        when (configuration.orientation) {
            Configuration.ORIENTATION_PORTRAIT -> configuration.screenWidthDp.dp.minus(120.dp).roundToPx()
            Configuration.ORIENTATION_LANDSCAPE -> configuration.screenHeightDp.dp.minus(120.dp).roundToPx()
            else -> 512
        }
    }

@Composable
fun QrCodeContent(
    modifier: Modifier = Modifier,
    qrCode: Bitmap? = null,
    isLoading: Boolean,
    isHelpEnabled: Boolean,
    onOtherMethodClicked: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 18.dp, top = 24.dp, start = 24.dp, end = 24.dp)
    ) {
        DashlaneLogoLockup(height = 40.dp)
        Text(
            text = (stringResource(id = R.string.login_secret_transfer_step_label, 1, 2)).uppercase(),
            style = DashlaneTheme.typography.bodyHelperRegular,
            color = DashlaneTheme.colors.textNeutralQuiet,
            modifier = Modifier
                .padding(top = 48.dp)
        )
        Text(
            text = stringResource(id = R.string.login_secret_transfer_qr_code_title),
            style = DashlaneTheme.typography.titleSectionLarge,
            color = DashlaneTheme.colors.textNeutralCatchy,
            modifier = Modifier
                .padding(top = 8.dp)
        )
        Spacer(modifier = Modifier.weight(1f))
        qrCode?.let { bitmap ->
            QRCode(
                qrCode = bitmap,
                modifier = Modifier.align(CenterHorizontally)
            )
        }
        if (isLoading) {
            IndeterminateLoader(
                modifier = Modifier
                    .align(CenterHorizontally)
                    .padding(top = 100.dp),
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        if (isHelpEnabled) {
            ButtonMedium(
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(top = 32.dp),
                onClick = onOtherMethodClicked,
                intensity = Intensity.Quiet,
                layout = ButtonLayout.TextOnly(
                    text = stringResource(id = R.string.login_secret_transfer_qr_code_help_button)
                )
            )
        }
    }
}

@Composable
fun QRCodeBottomSheetContent(
    onPrimaryButtonClick: () -> Unit,
    onSecondaryButtonClick: () -> Unit
) {
    Column(
        modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 64.dp)
    ) {
        Text(
            text = stringResource(id = R.string.login_secret_transfer_qr_code_help_sheet_title),
            style = DashlaneTheme.typography.titleSectionMedium,
            color = DashlaneTheme.colors.textNeutralCatchy,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        ButtonMediumBar(
            primaryText = stringResource(id = R.string.login_secret_transfer_qr_code_help_sheet_primary_button),
            secondaryText = stringResource(id = R.string.login_secret_transfer_qr_code_help_sheet_secondary_button),
            onPrimaryButtonClick = onPrimaryButtonClick,
            onSecondaryButtonClick = onSecondaryButtonClick
        )
    }
}

@Preview
@Composable
private fun QRCodeContentWithLoadingPreview() {
    DashlanePreview {
        QrCodeContent(
            isLoading = true,
            isHelpEnabled = true,
            onOtherMethodClicked = { },
        )
    }
}

@Preview
@Composable
private fun QRCodeContentWithQRPreview() {
    DashlanePreview {
        QrCodeContent(
            qrCode = generateQrCodeBitmap(
                uri = "dashlane.com".toUri(),
                size = getQrCodeSize(
                    density = LocalDensity.current,
                    configuration = LocalConfiguration.current
                ),
                color = DashlaneTheme.colors.textNeutralCatchy.value.toArgb()
            ),
            isLoading = false,
            isHelpEnabled = false,
            onOtherMethodClicked = { },
        )
    }
}

@Preview
@Composable
private fun QRCodeBottomSheetContentPreview() {
    DashlanePreview {
        QRCodeBottomSheetContent(
            onPrimaryButtonClick = {},
            onSecondaryButtonClick = {}
        )
    }
}
