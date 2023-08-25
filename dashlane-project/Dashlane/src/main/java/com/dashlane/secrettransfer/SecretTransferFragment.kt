package com.dashlane.secrettransfer

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContract
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.dashlane.R
import com.dashlane.barcodescanner.BarCodeCaptureActivity
import com.dashlane.design.component.ButtonLayout
import com.dashlane.design.component.ButtonMedium
import com.dashlane.design.component.Text
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.color.Intensity
import com.dashlane.navigation.Navigator
import com.dashlane.ui.activities.fragments.AbstractContentFragment
import com.dashlane.ui.widgets.compose.ButtonRow
import com.dashlane.ui.widgets.compose.LoadingScreen
import com.google.mlkit.vision.barcode.common.Barcode
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import javax.inject.Inject

@AndroidEntryPoint
class SecretTransferFragment : AbstractContentFragment() {

    @Inject
    lateinit var navigator: Navigator

    private val args: SecretTransferFragmentArgs by navArgs()
    private val viewModel by viewModels<SecretTransferViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                DashlaneTheme {
                    SecretTransferScreen(
                        viewModel = viewModel,
                        navigator = navigator,
                        deepLinkTransferId = args.id,
                        deepLinkKey = args.key
                    )
                }
            }
        }
    }
}

@Composable
fun SecretTransferScreen(
    viewModel: SecretTransferViewModel,
    navigator: Navigator,
    deepLinkTransferId: String?,
    deepLinkKey: String?
) {
    val scanQrCode = rememberLauncherForActivityResult(ScanQrCode) { secretTransferUri -> viewModel.qrScanned(secretTransferUri) }
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(key1 = deepLinkKey, key2 = deepLinkTransferId) {
        if (deepLinkKey != null && deepLinkTransferId != null) viewModel.deepLink(transferId = deepLinkTransferId, publicKey = deepLinkKey)
    }

    LaunchedEffect(key1 = uiState) {
        when (uiState) {
            SecretTransferState.Initial -> Unit
            SecretTransferState.Cancelled -> navigator.popBackStack()
            is SecretTransferState.Error -> Unit
            SecretTransferState.ScanningQR -> scanQrCode.launch(null)
            SecretTransferState.Success -> {
                delay(2_000) 
                navigator.popBackStack()
            }

            SecretTransferState.Loading -> Unit
        }
    }

    SecretTransferContent(
        onClickScan = viewModel::scanClicked,
        onClickCancel = viewModel::cancel,
        onClickRetry = viewModel::scanClicked,
        uiState = uiState
    )
}

@Composable
fun SecretTransferContent(
    modifier: Modifier = Modifier,
    onClickScan: () -> Unit,
    onClickCancel: () -> Unit,
    onClickRetry: () -> Unit,
    uiState: SecretTransferState,
) {
    Crossfade(targetState = uiState) { state ->
        when (state) {
            SecretTransferState.Cancelled -> Unit
            is SecretTransferState.Error -> SecretTransferError(modifier = Modifier, onClickCancel = onClickCancel, onClickRetry = onClickRetry)
            SecretTransferState.ScanningQR,
            SecretTransferState.Initial -> SecretTransferIntro(modifier = modifier, onClickScan = onClickScan)

            SecretTransferState.Loading -> LoadingScreen(modifier, stringResource(R.string.secret_transfer_screen_loading_title))
            SecretTransferState.Success -> SecretTransferSuccess()
        }
    }
}

@Composable
fun SecretTransferIntro(
    modifier: Modifier = Modifier,
    onClickScan: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .weight(1f)
                .padding(24.dp)
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
                    buildAnnotatedString { append(stringResource(id = R.string.secret_transfer_screen_step_2)) },
                    buildAnnotatedString { append(stringResource(id = R.string.secret_transfer_screen_step_3)) }
                )
            )
        }
        ButtonMedium(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, bottom = 24.dp),
            onClick = onClickScan,
            intensity = Intensity.Catchy,
            layout = ButtonLayout.TextOnly(
                text = stringResource(id = R.string.secret_transfer_screen_scan_cta)
            )
        )
    }
}

@Composable
fun SecretTransferSuccess(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .width(78.dp)
                .height(78.dp),
            painter = painterResource(id = R.drawable.ic_checklist_check),
            contentDescription = stringResource(id = R.string.and_accessibility_secret_transfer_success),
            colorFilter = ColorFilter.tint(DashlaneTheme.colors.textBrandQuiet.value)
        )
        Text(
            text = stringResource(id = R.string.secret_transfer_screen_success_title),
            style = DashlaneTheme.typography.titleSectionLarge,
            color = DashlaneTheme.colors.textNeutralCatchy,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 40.dp)
        )
    }
}

@Composable
fun SecretTransferError(
    modifier: Modifier = Modifier,
    onClickRetry: () -> Unit,
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
            Image(
                modifier = modifier.padding(top = 120.dp),
                painter = painterResource(R.drawable.ic_error_state),
                colorFilter = ColorFilter.tint(DashlaneTheme.colors.textDangerQuiet.value),
                contentDescription = ""
            )
            Text(
                text = stringResource(id = R.string.secret_transfer_screen_error_title),
                style = DashlaneTheme.typography.titleSectionLarge,
                color = DashlaneTheme.colors.textNeutralCatchy,
                modifier = Modifier.padding(top = 32.dp)
            )
            Text(
                text = stringResource(id = R.string.secret_transfer_screen_error_message),
                style = DashlaneTheme.typography.bodyStandardRegular,
                color = DashlaneTheme.colors.textNeutralStandard,
                modifier = Modifier.padding(top = 16.dp)
            )
        }

        ButtonRow(
            textPrimary = stringResource(id = R.string.secret_transfer_screen_error_retry_button),
            textSecondary = stringResource(id = R.string.secret_transfer_screen_error_cancel_button),
            onClickPrimary = onClickRetry,
            onClickSecondary = onClickCancel
        )
    }
}

@Composable
fun ContentStepper(
    modifier: Modifier = Modifier,
    content: List<AnnotatedString>
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                DashlaneTheme.colors.containerAgnosticNeutralSupershy,
                RoundedCornerShape(10.dp)
            )
            .padding(24.dp)
    ) {
        content.forEachIndexed { index, string ->
            val step = index + 1
            val bottomPadding = if (step == content.size) 0.dp else 16.dp
            Row(modifier = Modifier.padding(bottom = bottomPadding)) {
                ContentStepCircle(
                    text = step.toString(),
                    modifier = Modifier.align(CenterVertically)
                )
                Text(
                    text = string,
                    style = DashlaneTheme.typography.bodyStandardRegular,
                    color = DashlaneTheme.colors.textNeutralCatchy,
                    modifier = Modifier
                        .align(CenterVertically)
                        .padding(start = 12.dp)
                )
            }
        }
    }
}

@Composable
private fun ContentStepCircle(
    text: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(ContentStepCircleSize)
            .background(DashlaneTheme.colors.containerExpressiveBrandQuietIdle, CircleShape)
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            modifier = Modifier
                .width(ContentStepCircleSize)
                .heightIn(max = ContentStepCircleSize),
            text = text,
            style = DashlaneTheme.typography.bodyStandardRegular,
            color = DashlaneTheme.colors.textBrandStandard,
            textAlign = TextAlign.Center,
            overflow = TextOverflow.Visible
        )
    }
}

private val ContentStepCircleSize = 32.dp

@Preview
@Composable
fun SecretTransferIntroPreview() {
    DashlaneTheme { SecretTransferIntro(onClickScan = { }) }
}

@Preview
@Composable
fun SecretTransferErrorPreview() {
    DashlaneTheme { SecretTransferError(onClickRetry = {}, onClickCancel = {}) }
}

@Preview
@Composable
fun SecretTransferSuccessPreview() {
    DashlaneTheme { SecretTransferSuccess() }
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
