package com.dashlane.secrettransfer.view.intro

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContract
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dashlane.barcodescanner.BarCodeCaptureActivity
import com.dashlane.design.component.ButtonLayout
import com.dashlane.design.component.ButtonMedium
import com.dashlane.design.component.ButtonMediumBar
import com.dashlane.design.component.Text
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.color.Intensity
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.secrettransfer.R
import com.dashlane.secrettransfer.view.success.SecretTransferSuccess
import com.dashlane.ui.widgets.compose.ContentStepper
import com.dashlane.ui.widgets.compose.GenericErrorContent
import com.dashlane.ui.widgets.compose.LoadingScreen
import com.google.mlkit.vision.barcode.common.Barcode
import kotlinx.coroutines.delay

@Composable
fun SecretTransferIntroScreen(
    modifier: Modifier = Modifier,
    isPasswordless: Boolean = false,
    viewModel: SecretTransferIntroViewModel,
    onCancel: () -> Unit,
    onSuccess: () -> Unit,
    onRefresh: () -> Unit,
    deepLinkTransferId: String?,
    deepLinkKey: String?
) {
    val scanQrCode = rememberLauncherForActivityResult(ScanQrCode) { secretTransferUri -> viewModel.qrScanned(secretTransferUri) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(key1 = deepLinkKey, key2 = deepLinkTransferId) {
        if (deepLinkKey != null && deepLinkTransferId != null) viewModel.deepLink(transferId = deepLinkTransferId, publicKey = deepLinkKey)
    }

    LaunchedEffect(key1 = uiState) {
        when (uiState) {
            SecretTransferIntroState.Initial -> Unit
            SecretTransferIntroState.Cancelled -> onCancel()
            is SecretTransferIntroState.Error -> Unit
            SecretTransferIntroState.ScanningQR -> scanQrCode.launch(null)
            SecretTransferIntroState.Success -> {
                delay(2_000) 
                onSuccess()
            }

            SecretTransferIntroState.Loading -> Unit
        }
    }

    Crossfade(targetState = uiState, label = "secretTransferCrossfade") { state ->
        when (state) {
            SecretTransferIntroState.Cancelled -> Unit
            is SecretTransferIntroState.Error -> {
                GenericErrorContent(
                    modifier = modifier,
                    title = stringResource(id = R.string.secret_transfer_screen_error_title),
                    message = stringResource(id = R.string.secret_transfer_screen_error_message),
                    textPrimary = stringResource(id = R.string.secret_transfer_screen_error_retry_button),
                    textSecondary = stringResource(id = R.string.secret_transfer_screen_error_cancel_button),
                    onClickPrimary = viewModel::scanClicked,
                    onClickSecondary = onCancel
                )
            }
            SecretTransferIntroState.ScanningQR,
            SecretTransferIntroState.Initial -> {
                if (isPasswordless) {
                    SecretTransferIntroPasswordLessContent(
                        modifier = modifier,
                        onClickScan = viewModel::scanClicked,
                        onClickRefresh = onRefresh
                    )
                } else {
                    SecretTransferIntroContent(modifier = modifier, onClickScan = viewModel::scanClicked)
                }
            }

            SecretTransferIntroState.Loading -> LoadingScreen(modifier, stringResource(R.string.secret_transfer_screen_loading_title))
            SecretTransferIntroState.Success -> SecretTransferSuccess()
        }
    }
}

@Composable
fun SecretTransferIntroContent(
    modifier: Modifier = Modifier,
    onClickScan: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 18.dp, top = 24.dp, start = 24.dp, end = 24.dp)
    ) {
        Text(
            text = stringResource(id = R.string.secret_transfer_screen_title),
            style = DashlaneTheme.typography.titleSectionLarge,
            color = DashlaneTheme.colors.textNeutralCatchy,
            modifier = Modifier
                .padding(bottom = 32.dp)
        )
        ContentStepper(
            content = listOf(
                buildAnnotatedString {
                    append(stringResource(id = R.string.secret_transfer_screen_step_1))
                    append(" ")
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(stringResource(id = R.string.secret_transfer_screen_step_1_bold))
                    }
                },
                buildAnnotatedString { append(stringResource(R.string.secret_transfer_screen_step_2)) },
                buildAnnotatedString { append(stringResource(id = R.string.secret_transfer_screen_step_3)) }
            )
        )

        Spacer(modifier = Modifier.weight(1f))

        ButtonMedium(
            modifier = Modifier
                .align(Alignment.End),
            onClick = onClickScan,
            intensity = Intensity.Catchy,
            layout = ButtonLayout.TextOnly(
                text = stringResource(id = R.string.secret_transfer_screen_scan_cta)
            )
        )
    }
}

@Composable
fun SecretTransferIntroPasswordLessContent(
    modifier: Modifier = Modifier,
    onClickScan: () -> Unit,
    onClickRefresh: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 18.dp, top = 24.dp, start = 24.dp, end = 24.dp)
    ) {
        Text(
            text = stringResource(id = R.string.secret_transfer_universal_screen_title),
            style = DashlaneTheme.typography.titleSectionLarge,
            color = DashlaneTheme.colors.textNeutralCatchy,
            modifier = Modifier
                .padding(bottom = 32.dp)
        )

        ContentStepper(
            content = listOf(
                stringResource(R.string.secret_transfer_universal_screen_step_1),
                stringResource(R.string.secret_transfer_universal_screen_step_2),
                stringResource(R.string.secret_transfer_universal_screen_step_3)
            )
        )

        Spacer(modifier = Modifier.weight(1f))

        ButtonMediumBar(
            primaryText = stringResource(id = R.string.secret_transfer_screen_scan_cta),
            onPrimaryButtonClick = onClickScan,
            secondaryText = stringResource(id = R.string.secret_transfer_universal_screen_refresh_cta),
            onSecondaryButtonClick = onClickRefresh
        )
    }
}

@Preview
@Composable
fun SecretTransferIntroPreview() {
    DashlanePreview { SecretTransferIntroContent(onClickScan = { }) }
}

@Preview
@Composable
fun SecretTransferIntroPasswordlessPreview() {
    DashlanePreview { SecretTransferIntroPasswordLessContent(onClickScan = { }, onClickRefresh = { }) }
}

private object ScanQrCode : ActivityResultContract<Unit?, String?>() {
    @SuppressLint("UnsafeOptInUsageError")
    override fun createIntent(context: Context, input: Unit?): Intent {
        return Intent(context, BarCodeCaptureActivity::class.java)
            .putExtra(BarCodeCaptureActivity.HEADER, context.resources.getString(R.string.secret_transfer_scanning_screen_title))
            .putExtra(BarCodeCaptureActivity.BARCODE_FORMAT, Barcode.FORMAT_QR_CODE)
    }

    @SuppressLint("UnsafeOptInUsageError")
    override fun parseResult(resultCode: Int, intent: Intent?): String? {
        if (resultCode != Activity.RESULT_OK || intent == null) return null
        val barcode = intent.getStringArrayExtra(BarCodeCaptureActivity.RESULT_EXTRA_BARCODE_VALUES)
        return barcode?.get(0)
    }
}
