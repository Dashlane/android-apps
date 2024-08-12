package com.dashlane.secrettransfer.view.universal.pending

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dashlane.design.component.ButtonLayout
import com.dashlane.design.component.ButtonMediumBar
import com.dashlane.design.component.Dialog
import com.dashlane.design.component.InfoboxMedium
import com.dashlane.design.component.Text
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.color.Mood
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.secrettransfer.R
import com.dashlane.secrettransfer.view.SecretTransfer
import com.dashlane.secrettransfer.view.success.SecretTransferSuccess
import com.dashlane.secrettransfer.view.universal.passphrase.PassphraseVerificationScreen
import com.dashlane.ui.common.compose.components.LoadingScreen
import com.dashlane.ui.widgets.compose.GenericErrorContent
import com.dashlane.util.SnackbarUtils
import com.dashlane.util.getBaseActivity
import kotlinx.coroutines.delay

@Composable
@Suppress("LongMethod")
fun SecretTransferPendingScreen(
    modifier: Modifier = Modifier,
    viewModel: SecretTransferPendingViewModel,
    secretTransfer: SecretTransfer,
    onCancel: () -> Unit,
    onSuccess: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    BackHandler {
        viewModel.onBackPressed()
    }

    LaunchedEffect(key1 = uiState) {
        when (uiState) {
            is SecretTransferPendingState.Reject -> {
                context.getBaseActivity()?.let { activity ->
                    SnackbarUtils.showSnackbar(
                        activity,
                        context.getString(R.string.secret_transfer_universal_pending_screen_reject_toast)
                    )
                }
                onCancel()
            }
            is SecretTransferPendingState.Success -> {
                delay(2_000) 
                onSuccess()
            }
            is SecretTransferPendingState.Cancelled -> onCancel()
            else -> Unit
        }
    }

    when (val state = uiState) {
        is SecretTransferPendingState.Error -> SecretTransferPendingErrorContent(error = state.error, onCancel = onCancel)
        is SecretTransferPendingState.Reject,
        is SecretTransferPendingState.Initial -> {
            SecretTransferPendingContent(
                modifier = modifier,
                secretTransfer = secretTransfer,
                onCancel = viewModel::rejectTransfer,
                onConfirm = viewModel::confirmTransfer
            )
        }
        is SecretTransferPendingState.CancelPassphrase,
        is SecretTransferPendingState.PassphraseVerification -> {
            PassphraseVerificationScreen(
                modifier = modifier,
                deviceName = state.data.transfer?.deviceName ?: "",
                passphrase = state.data.passphrase ?: emptyList(),
                onConfirm = viewModel::completeTransfer,
                onValueChange = viewModel::missingWordValueChanged
            )
            if (state is SecretTransferPendingState.CancelPassphrase) {
                CancelPassphraseDialog(
                    onConfirm = viewModel::cancelConfirm,
                    onDismiss = viewModel::cancelDismiss
                )
            }
        }

        is SecretTransferPendingState.LoadingChallenge -> LoadingScreen(
            modifier,
            stringResource(R.string.secret_transfer_universal_screen_loading_challenge)
        )
        is SecretTransferPendingState.LoadingAccount -> LoadingScreen(modifier, stringResource(R.string.secret_transfer_screen_loading_title))
        is SecretTransferPendingState.Success -> SecretTransferSuccess()
        is SecretTransferPendingState.Cancelled -> Unit
    }
}

@Composable
@Suppress("LongMethod")
fun SecretTransferPendingContent(
    modifier: Modifier = Modifier,
    secretTransfer: SecretTransfer,
    onCancel: () -> Unit,
    onConfirm: (SecretTransfer) -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 18.dp, top = 24.dp, start = 24.dp, end = 24.dp)
    ) {
        Text(
            text = stringResource(id = R.string.secret_transfer_universal_pending_screen_title),
            style = DashlaneTheme.typography.titleSectionLarge,
            color = DashlaneTheme.colors.textNeutralCatchy,
            modifier = Modifier
                .padding(top = 40.dp, bottom = 16.dp)
        )
        Text(
            text = stringResource(id = R.string.secret_transfer_universal_pending_screen_description),
            style = DashlaneTheme.typography.bodyStandardRegular,
            color = DashlaneTheme.colors.textNeutralCatchy,
            modifier = Modifier
                .padding(bottom = 16.dp)
        )
        Column(
            modifier = modifier
                .background(
                    color = DashlaneTheme.colors.containerAgnosticNeutralSupershy,
                    shape = RoundedCornerShape(10.dp)
                )
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                text = secretTransfer.deviceName,
                style = DashlaneTheme.typography.titleBlockMedium,
                color = DashlaneTheme.colors.textNeutralCatchy,
                modifier = Modifier
                    .padding(bottom = 12.dp)
            )
            Text(
                text = secretTransfer.city + " " + secretTransfer.countryCode,
                style = DashlaneTheme.typography.titleBlockMedium,
                color = DashlaneTheme.colors.textNeutralCatchy,
                modifier = Modifier
                    .padding(bottom = 12.dp)
            )
            Text(
                text = secretTransfer.formattedDate,
                style = DashlaneTheme.typography.titleBlockMedium,
                color = DashlaneTheme.colors.textNeutralCatchy,
            )
        }

        InfoboxMedium(
            modifier = Modifier.padding(top = 24.dp),
            title = stringResource(id = R.string.secret_transfer_universal_pending_screen_infobox),
            mood = Mood.Neutral
        )

        Spacer(modifier = Modifier.weight(1f))

        ButtonMediumBar(
            modifier = Modifier.padding(top = 18.dp),
            primaryText = stringResource(id = R.string.secret_transfer_universal_pending_screen_confirm_button),
            onPrimaryButtonClick = { onConfirm(secretTransfer) },
            secondaryText = stringResource(id = R.string.secret_transfer_universal_pending_screen_reject_button),
            onSecondaryButtonClick = onCancel
        )
    }
}

@Composable
fun SecretTransferPendingErrorContent(
    error: SecretTransferPendingError,
    onCancel: () -> Unit
) {
    when (error) {
        SecretTransferPendingError.Generic -> {
            GenericErrorContent(
                textPrimary = stringResource(id = R.string.secret_transfer_universal_screen_error_cta),
                onClickPrimary = onCancel,
            )
        }
        SecretTransferPendingError.Timeout -> {
            GenericErrorContent(
                title = stringResource(id = R.string.secret_transfer_universal_pending_timeout_error_screen_title),
                message = stringResource(id = R.string.secret_transfer_universal_pending_timeout_error_screen_description),
                textPrimary = stringResource(id = R.string.secret_transfer_universal_screen_error_cta),
                onClickPrimary = onCancel,
            )
        }
        SecretTransferPendingError.PassphraseMaxTries -> {
            GenericErrorContent(
                title = stringResource(id = R.string.secret_transfer_universal_pending_max_passphrase_tries_error_screen_title),
                message = stringResource(id = R.string.secret_transfer_universal_pending_max_passphrase_tries_error_screen_description),
                textPrimary = stringResource(id = R.string.secret_transfer_universal_screen_error_cta),
                onClickPrimary = onCancel,
            )
        }
    }
}

@Composable
fun CancelPassphraseDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        title = stringResource(id = R.string.secret_transfer_universal_passphrase_verification_cancel_dialog_title),
        description = {
            Text(text = stringResource(id = R.string.secret_transfer_universal_passphrase_verification_cancel_dialog_description))
        },
        mainActionLayout = ButtonLayout.TextOnly(stringResource(id = R.string.secret_transfer_universal_passphrase_verification_cancel_positive_button)),
        mainActionClick = onConfirm,
        additionalActionLayout = ButtonLayout.TextOnly(stringResource(id = R.string.secret_transfer_universal_passphrase_verification_cancel_negative_button)),
        additionalActionClick = onDismiss,
        onDismissRequest = { },
        isDestructive = true
    )
}

@Preview
@Composable
fun SecretTransferPendingScreenPreview() {
    DashlanePreview {
        SecretTransferPendingContent(
            secretTransfer = SecretTransfer(
                id = "id",
                deviceName = "Device Name",
                city = "City",
                countryCode = "FR",
                formattedDate = "Oct 13, 2023, 12:39 PM",
                hashedPublicKey = ""
            ),
            onCancel = {},
            onConfirm = {}
        )
    }
}

@Preview
@Composable
fun SecretTransferPendingErrorScreenPreview() {
    DashlanePreview {
        SecretTransferPendingErrorContent(
            error = SecretTransferPendingError.Timeout,
            onCancel = {},
        )
    }
}

@Preview
@Composable
fun CancelPassphraseDialogPreview() {
    DashlanePreview {
        CancelPassphraseDialog(
            onConfirm = {},
            onDismiss = {}
        )
    }
}
