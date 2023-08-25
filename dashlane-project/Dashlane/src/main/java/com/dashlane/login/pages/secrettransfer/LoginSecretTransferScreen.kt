package com.dashlane.login.pages.secrettransfer

import android.content.res.Configuration
import android.graphics.Bitmap
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.dashlane.R
import com.dashlane.design.component.Text
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.login.LoginIntents
import com.dashlane.ui.widgets.compose.ButtonRow
import com.dashlane.ui.widgets.compose.LoadingScreen
import com.dashlane.ui.widgets.view.CircularProgressIndicator
import com.dashlane.util.getBaseActivity
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter

@Composable
fun LoginSecretTransferScreen(
    modifier: Modifier = Modifier,
    viewModel: LoginSecretTransferViewModel,
    navController: NavController
) {
    val activity = LocalContext.current.getBaseActivity()
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.viewStarted()
    }

    LaunchedEffect(key1 = uiState) {
        when (uiState) {
            is LoginSecretTransferState.Error,
            is LoginSecretTransferState.LoadingQR,
            is LoginSecretTransferState.ConfirmEmail,
            is LoginSecretTransferState.LoadingLogin,
            is LoginSecretTransferState.AskForTOTP,
            is LoginSecretTransferState.WaitForPush,
            is LoginSecretTransferState.QrCodeUriGenerated -> Unit

            is LoginSecretTransferState.LoginSuccess -> {
                activity?.run {
                    startActivity(LoginIntents.createProgressActivityIntent(this))
                    finish()
                }
            }

            is LoginSecretTransferState.Cancelled -> navController.popBackStack()
        }
    }

    LoginQRContent(
        modifier = modifier,
        uiState = uiState,
        onClickConfirm = viewModel::emailConfirmed,
        onClickRetry = viewModel::retry,
        onClickCancel = viewModel::cancel,
        onClickCancelOnError = viewModel::cancelOnError,
        onOTPComplete = viewModel::totpCompleted,
        onClickUse2FACode = viewModel::changeFromPushTo2FA
    )
}

@Composable
fun LoginQRContent(
    modifier: Modifier = Modifier,
    uiState: LoginSecretTransferState,
    onClickConfirm: () -> Unit,
    onClickRetry: (LoginSecretTransferError) -> Unit,
    onClickCancel: () -> Unit,
    onClickCancelOnError: (LoginSecretTransferError) -> Unit,
    onOTPComplete: (String) -> Unit,
    onClickUse2FACode: () -> Unit
) {
    Crossfade(targetState = uiState) { state ->
        when (state) {
            is LoginSecretTransferState.ConfirmEmail -> LoginStepConfirmEmail(
                modifier = modifier,
                email = state.email,
                onClickConfirm = onClickConfirm,
                onClickCancel = onClickCancel
            )

            is LoginSecretTransferState.Error -> LoginError(
                error = state.error,
                onClickRetry = { onClickRetry(state.error) },
                onClickCancel = { onClickCancelOnError(state.error) }
            )

            is LoginSecretTransferState.LoadingQR -> LoginStepQRCode(
                modifier = modifier,
                isLoading = true
            )

            is LoginSecretTransferState.QrCodeUriGenerated -> LoginStepQRCode(
                modifier = modifier,
                qrCodeUri = state.data.qrCodeUri,
                isLoading = false
            )

            is LoginSecretTransferState.LoadingLogin -> LoadingScreen(modifier, stringResource(R.string.login_secret_transfer_loading_login_title))
            is LoginSecretTransferState.Cancelled,
            is LoginSecretTransferState.LoginSuccess -> Unit

            is LoginSecretTransferState.AskForTOTP -> LoginTotpContent(modifier = modifier, onOTPComplete)
            is LoginSecretTransferState.WaitForPush -> LoginAuthenticatorPushContent(modifier = modifier, onClickUse2FACode)
        }
    }
}

@Composable
fun LoginStepQRCode(
    modifier: Modifier = Modifier,
    qrCodeUri: String? = null,
    isLoading: Boolean
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        DashlaneLogo()
        Text(
            text = (stringResource(id = R.string.login_secret_transfer_step_label, 1, 2)).uppercase(),
            style = DashlaneTheme.typography.titleSupportingSmall,
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
                    .padding(top = 60.dp)
            )
        }
        if (isLoading) {
            CircularProgressIndicator(
                color = DashlaneTheme.colors.textBrandQuiet.value,
                modifier = Modifier
                    .align(CenterHorizontally)
                    .padding(top = 60.dp)
            )
        }
    }
}

@Composable
fun LoginStepConfirmEmail(
    modifier: Modifier = Modifier,
    email: String,
    onClickConfirm: () -> Unit,
    onClickCancel: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            DashlaneLogo()
            Text(
                text = (stringResource(id = R.string.login_secret_transfer_step_label, 2, 2)).uppercase(),
                style = DashlaneTheme.typography.titleSupportingSmall,
                color = DashlaneTheme.colors.textNeutralQuiet,
                modifier = Modifier.padding(top = 48.dp)
            )
            Text(
                text = stringResource(id = R.string.login_secret_transfer_confirm_email_title),
                style = DashlaneTheme.typography.titleSectionLarge,
                color = DashlaneTheme.colors.textNeutralCatchy,
                modifier = Modifier.padding(top = 8.dp)
            )
            Text(
                text = stringResource(id = R.string.login_secret_transfer_confirm_email_body),
                style = DashlaneTheme.typography.bodyStandardRegular,
                color = DashlaneTheme.colors.textNeutralStandard,
                modifier = Modifier.padding(top = 16.dp)
            )
            Text(
                text = email,
                style = DashlaneTheme.typography.bodyStandardRegular,
                color = DashlaneTheme.colors.textNeutralCatchy,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
        ButtonRow(
            textPrimary = stringResource(id = R.string.login_secret_transfer_confirm_email_button_confirm),
            textSecondary = stringResource(id = R.string.login_secret_transfer_confirm_email_button_cancel),
            onClickPrimary = onClickConfirm,
            onClickSecondary = onClickCancel
        )
    }
}

@Composable
fun LoginError(
    modifier: Modifier = Modifier,
    error: LoginSecretTransferError,
    onClickRetry: () -> Unit,
    onClickCancel: () -> Unit
) {
    val (title, message) = when (error) {
        LoginSecretTransferError.LoginError -> stringResource(id = R.string.login_secret_transfer_login_error_title) to
            stringResource(id = R.string.login_secret_transfer_login_error_message)

        LoginSecretTransferError.QrCodeGeneration,
        LoginSecretTransferError.StartTransferError -> stringResource(id = R.string.login_secret_transfer_data_error_title) to
            stringResource(id = R.string.login_secret_transfer_data_error_message)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Image(
                modifier = modifier.padding(top = 120.dp),
                painter = painterResource(R.drawable.ic_error_state),
                colorFilter = ColorFilter.tint(DashlaneTheme.colors.textDangerQuiet.value),
                contentDescription = ""
            )
            Text(
                text = title,
                style = DashlaneTheme.typography.titleSectionLarge,
                color = DashlaneTheme.colors.textNeutralCatchy,
                modifier = Modifier.padding(top = 32.dp)
            )
            Text(
                text = message,
                style = DashlaneTheme.typography.bodyStandardRegular,
                color = DashlaneTheme.colors.textNeutralStandard,
                modifier = Modifier.padding(top = 16.dp)
            )
        }

        ButtonRow(
            textPrimary = stringResource(id = R.string.login_secret_transfer_error_button_retry),
            textSecondary = stringResource(id = R.string.login_secret_transfer_error_button_cancel),
            onClickPrimary = onClickRetry,
            onClickSecondary = onClickCancel
        )
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
    DashlaneTheme(darkTheme = true) {
        LoginStepQRCode(
            isLoading = true
        )
    }
}

@Preview
@Composable
fun LoginStepQRCodeWithQRPreview() {
    DashlaneTheme(darkTheme = true) {
        LoginStepQRCode(
            qrCodeUri = "dashlane.com",
            isLoading = false
        )
    }
}

@Preview
@Composable
fun LoginStepConfirmEmailPreview() {
    DashlaneTheme(darkTheme = true) {
        LoginStepConfirmEmail(
            email = "dashlane.com",
            onClickCancel = {},
            onClickConfirm = {}
        )
    }
}

@Preview
@Composable
fun LoginErrorPreview() {
    DashlaneTheme(darkTheme = true) {
        LoginError(
            error = LoginSecretTransferError.LoginError,
            onClickCancel = {},
            onClickRetry = {}
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
