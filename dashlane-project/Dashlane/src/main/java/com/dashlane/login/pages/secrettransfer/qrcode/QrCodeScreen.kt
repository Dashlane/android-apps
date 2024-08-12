package com.dashlane.login.pages.secrettransfer.qrcode

import android.content.res.Configuration
import android.graphics.Bitmap
import androidx.compose.foundation.Image
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dashlane.R
import com.dashlane.authentication.RegisteredUserDevice
import com.dashlane.design.component.ButtonLayout
import com.dashlane.design.component.ButtonMedium
import com.dashlane.design.component.ButtonMediumBar
import com.dashlane.design.component.Text
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.color.Intensity
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.secrettransfer.domain.SecretTransferPayload
import com.dashlane.ui.widgets.compose.DashlaneLogo
import com.dashlane.ui.widgets.compose.GenericErrorContent
import com.dashlane.ui.widgets.view.CircularProgressIndicator
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter

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
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.viewStarted(email)
    }

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is QrCodeState.GoToConfirmEmail -> {
                viewModel.viewNavigated()
                onQrScanned(state.secretTransferPayload)
            }

            is QrCodeState.Cancelled -> onCancelled()
            is QrCodeState.GoToARK -> {
                viewModel.viewNavigated()
                onGoToARK(state.registeredUserDevice)
            }
            is QrCodeState.GoToUniversalD2D -> {
                viewModel.viewNavigated()
                onGoToUniversalD2D(state.email)
            }
            else -> Unit
        }
    }

    when (val state = uiState) {
        is QrCodeState.Error -> {
            GenericErrorContent(
                title = stringResource(id = R.string.login_secret_transfer_data_error_title),
                message = stringResource(id = R.string.login_secret_transfer_data_error_message),
                textPrimary = stringResource(id = R.string.login_secret_transfer_error_button_retry),
                textSecondary = stringResource(id = R.string.login_secret_transfer_error_button_cancel),
                onClickPrimary = { viewModel.retry(state.error) },
                onClickSecondary = { viewModel.cancelOnError(state.error) }
            )
        }
        else -> QrCodeContent(
            modifier = modifier,
            qrCodeUri = state.data.qrCodeUri,
            isLoading = state is QrCodeState.LoadingQR,
            isHelpEnabled = state.data.email != null,
            onOtherMethodClicked = viewModel::helpClicked
        )
    }

    if (uiState.data.bottomSheetVisible) {
        val sheetState = rememberModalBottomSheetState(
            confirmValueChange = { sheetValue ->
                if (sheetValue == SheetValue.Hidden) viewModel.bottomSheetDismissed()
                true
            }
        )

        ModalBottomSheet(
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

@Composable
fun QrCodeContent(
    modifier: Modifier = Modifier,
    qrCodeUri: String? = null,
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
        DashlaneLogo(color = DashlaneTheme.colors.oddityBrand)
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
        qrCodeUri?.let { uri ->
            QRCode(
                uri = uri,
                modifier = Modifier.align(CenterHorizontally)
            )
        }
        if (isLoading) {
            CircularProgressIndicator(
                color = DashlaneTheme.colors.textBrandQuiet.value,
                modifier = Modifier
                    .align(CenterHorizontally)
                    .padding(top = 100.dp)
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
        modifier = Modifier.padding(24.dp)
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

@Composable
fun QRCode(
    modifier: Modifier = Modifier,
    uri: String
) {
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    
    val size = with(density) {
        when (configuration.orientation) {
            Configuration.ORIENTATION_PORTRAIT -> configuration.screenWidthDp.dp.minus(120.dp).roundToPx()
            Configuration.ORIENTATION_LANDSCAPE -> configuration.screenHeightDp.dp.minus(120.dp).roundToPx()
            else -> 512
        }
    }
    val imageBitmap = generateBitmapFromUri(
        secretTransferUri = uri,
        size = size,
        
        color = DashlaneTheme.colors.textNeutralCatchy.value.toArgb()
    )

    Image(
        bitmap = imageBitmap,
        contentDescription = stringResource(id = R.string.and_accessibility_secret_transfer_qrcode),
        modifier = modifier
    )
}

@Preview
@Composable
fun QRCodeContentWithLoadingPreview() {
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
fun QRCodeContentWithQRPreview() {
    DashlanePreview {
        QrCodeContent(
            qrCodeUri = "dashlane.com",
            isLoading = false,
            isHelpEnabled = false,
            onOtherMethodClicked = { },
        )
    }
}

@Preview
@Composable
fun QRCodeBottomSheetContentPreview() {
    DashlanePreview {
        QRCodeBottomSheetContent(
            onPrimaryButtonClick = {},
            onSecondaryButtonClick = {}
        )
    }
}

private fun generateBitmapFromUri(
    secretTransferUri: String,
    size: Int,
    color: Int
): ImageBitmap {
    val writer = QRCodeWriter()
    val bitMatrix = writer.encode(secretTransferUri, BarcodeFormat.QR_CODE, size, size)
    val width = bitMatrix.width
    val height = bitMatrix.height
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    for (x in 0 until width) {
        for (y in 0 until height) {
            bitmap.setPixel(x, y, if (bitMatrix[x, y]) color else Color.Transparent.toArgb())
        }
    }
    return bitmap.asImageBitmap()
}
