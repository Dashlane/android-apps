package com.dashlane.login.pages.secrettransfer.qrcode

import android.content.res.Configuration
import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dashlane.R
import com.dashlane.authentication.RegisteredUserDevice
import com.dashlane.design.component.ButtonLayout
import com.dashlane.design.component.ButtonMedium
import com.dashlane.design.component.Text
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.color.Intensity
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.login.pages.secrettransfer.SecretTransferPayload
import com.dashlane.ui.widgets.compose.GenericErrorContent
import com.dashlane.ui.widgets.view.CircularProgressIndicator
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter

@Composable
fun QrCodeScreen(
    modifier: Modifier = Modifier,
    viewModel: QrCodeViewModel,
    email: String? = null,
    onQrScanned: (SecretTransferPayload) -> Unit,
    onGoToARK: (RegisteredUserDevice) -> Unit,
    onCancelled: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.viewStarted(email)
    }

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is QrCodeState.GoToConfirmEmail -> {
                viewModel.hasNavigated()
                onQrScanned(state.secretTransferPayload)
            }

            is QrCodeState.Cancelled -> onCancelled()
            is QrCodeState.GoToARK -> {
                viewModel.hasNavigated()
                onGoToARK(state.registeredUserDevice)
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

        is QrCodeState.LoadingQR -> QrCodeContent(modifier = modifier, isLoading = true, arkEnabled = state.data.arkEnabled, onARKClicked = { })

        is QrCodeState.GoToConfirmEmail,
        is QrCodeState.QrCodeUriGenerated -> QrCodeContent(
            modifier = modifier,
            qrCodeUri = state.data.qrCodeUri,
            isLoading = false,
            arkEnabled = state.data.arkEnabled,
            onARKClicked = viewModel::arkClicked
        )

        is QrCodeState.Initial,
        is QrCodeState.GoToARK,
        is QrCodeState.Cancelled -> Unit
    }
}

@Composable
fun QrCodeContent(
    modifier: Modifier = Modifier,
    qrCodeUri: String? = null,
    isLoading: Boolean,
    arkEnabled: Boolean,
    onARKClicked: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        DashlaneLogo()
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
        qrCodeUri?.let { uri ->
            QRCode(
                uri = uri,
                modifier = Modifier
                    .align(CenterHorizontally)
                    .padding(top = 60.dp, bottom = 60.dp)
            )
        }
        if (arkEnabled) {
            ButtonMedium(
                modifier = Modifier
                    .align(CenterHorizontally)
                    .padding(bottom = 40.dp),
                onClick = onARKClicked,
                intensity = Intensity.Supershy,
                layout = ButtonLayout.TextOnly(
                    text = stringResource(id = R.string.login_password_dialog_trouble_recovery_key_button)
                )
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
    }
}

@Composable
fun DashlaneLogo(
    modifier: Modifier = Modifier
) {
    Image(
        modifier = modifier,
        painter = painterResource(R.drawable.logo_lock_up),
        colorFilter = ColorFilter.tint(DashlaneTheme.colors.textNeutralCatchy.value),
        contentDescription = stringResource(id = R.string.and_accessibility_domain_item_logo, stringResource(id = R.string.dashlane_main_app_name))
    )
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
fun LoginStepQRCodeWithLoadingPreview() {
    DashlanePreview {
        QrCodeContent(
            isLoading = true,
            arkEnabled = false,
            onARKClicked = { }
        )
    }
}

@Preview
@Composable
fun LoginStepQRCodeWithQRPreview() {
    DashlanePreview {
        QrCodeContent(
            qrCodeUri = "dashlane.com",
            isLoading = false,
            arkEnabled = true,
            onARKClicked = { }
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
            bitmap.setPixel(x, y, if (bitMatrix.get(x, y)) color else Color.Transparent.toArgb())
        }
    }
    return bitmap.asImageBitmap()
}
