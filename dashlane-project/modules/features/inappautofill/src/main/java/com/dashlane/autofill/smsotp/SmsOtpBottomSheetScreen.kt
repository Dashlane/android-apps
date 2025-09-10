package com.dashlane.autofill.smsotp

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dashlane.autofill.api.R
import com.dashlane.autofill.ui.DashlaneAutofillDecoration
import com.dashlane.design.component.BottomSheetActions
import com.dashlane.design.component.BottomSheetContent
import com.dashlane.design.component.IndeterminateLoader
import com.dashlane.design.component.ModalBottomSheet
import com.dashlane.design.component.Text
import com.dashlane.design.component.tooling.DashlaneModalBottomSheetPreview
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.ui.common.compose.components.StatusBarBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmsOtpBottomSheetScreen(
    viewModel: SmsOtpAutofillViewModel,
    onSuccess: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    LaunchedEffect(viewModel) {
        viewModel.stateFlow.sideEffect.collect { sideEffect ->
            when (sideEffect) {
                is SmsOtpState.SideEffect.OnSuccess -> onSuccess(sideEffect.smsCode)
                SmsOtpState.SideEffect.OnCancel -> onDismiss()
            }
        }
    }

    val uiState by viewModel.stateFlow.viewState.collectAsStateWithLifecycle()
    when (val viewState = uiState) {
        SmsOtpState.View.Initial -> {
            
        }

        is SmsOtpState.View.Shown -> {
            Box {
                StatusBarBackground()
                ModalBottomSheet(
                    onDismissRequest = viewModel::onDismiss,
                ) {
                    when (viewState) {
                        SmsOtpState.View.Shown.Loading -> {
                            SmsOtpLoadingBottomSheetContent(
                                onCancelClick = viewModel::onDismiss,
                            )
                        }

                        is SmsOtpState.View.Shown.DisplayCode -> {
                            SmsOtpDisplayCodeBottomSheetContent(
                                code = viewState.smsCode,
                                onAutofillClick = viewModel::onAutofillClick,
                                onCancelClick = viewModel::onDismiss,
                            )
                        }

                        is SmsOtpState.View.Shown.Error -> {
                            SmsOtpErrorBottomSheetContent(
                                message = viewState.message,
                                onCancelClick = viewModel::onDismiss,
                            )
                        }
                    }
                }
            }
        }

        SmsOtpState.View.Hidden -> {
            
        }
    }
}

@Composable
private fun SmsOtpLoadingBottomSheetContent(
    onCancelClick: () -> Unit,
) {
    BaseBottomSheetContent(
        title = stringResource(R.string.autofill_sms_otp_dialog_waiting_title),
        content = {
            IndeterminateLoader(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally),
            )
        },
        onAutofillClick = null,
        onCancelClick = onCancelClick,
    )
}

@Composable
private fun SmsOtpDisplayCodeBottomSheetContent(
    code: String,
    onAutofillClick: (String) -> Unit,
    onCancelClick: () -> Unit,
) {
    BaseBottomSheetContent(
        title = stringResource(R.string.autofill_sms_otp_dialog_success_title),
        content = {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .clickable(
                        onClickLabel = stringResource(R.string.and_accessibility_autofill_sms_otp_dialog_code_click_label),
                        onClick = { onAutofillClick(code) },
                    )
                    .wrapContentHeight(align = Alignment.CenterVertically),
                text = code,
                style = DashlaneTheme.typography.specialtyMonospaceSmall,
                color = DashlaneTheme.colors.textNeutralStandard,
                textAlign = TextAlign.Center,
            )
        },
        onAutofillClick = { onAutofillClick(code) },
        onCancelClick = onCancelClick,
    )
}

@Composable
private fun SmsOtpErrorBottomSheetContent(
    message: String,
    onCancelClick: () -> Unit,
) {
    BaseBottomSheetContent(
        title = stringResource(R.string.autofill_sms_otp_dialog_error_title),
        content = {
            Text(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally),
                text = message,
                style = DashlaneTheme.typography.bodyStandardRegular,
                color = DashlaneTheme.colors.textNeutralStandard,
            )
        },
        onAutofillClick = null,
        onCancelClick = onCancelClick,
    )
}

@Composable
private fun BaseBottomSheetContent(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
    onAutofillClick: (() -> Unit)?,
    onCancelClick: () -> Unit,
) {
    BottomSheetContent(
        decoration = { DashlaneAutofillDecoration() },
        title = title,
        description = null,
        extraContent = content,
        actions = BottomSheetActions(
            primaryButtonText = stringResource(R.string.autofill_sms_otp_dialog_autofill_button)
                .takeIf { onAutofillClick != null },
            onPrimaryButtonClick = onAutofillClick,
            secondaryButtonText = stringResource(R.string.autofill_sms_otp_dialog_cancel_button),
            onSecondaryButtonClick = onCancelClick,
        ),
    )
}

@Preview
@Composable
private fun SmsOtpLoadingBottomSheetContentPreview() {
    DashlaneModalBottomSheetPreview {
        SmsOtpLoadingBottomSheetContent(
            onCancelClick = {},
        )
    }
}

@Preview
@Composable
private fun SmsOtpDisplayCodeBottomSheetContentPreview() {
    DashlaneModalBottomSheetPreview {
        SmsOtpDisplayCodeBottomSheetContent(
            code = "845763",
            onAutofillClick = {},
            onCancelClick = {},
        )
    }
}

@Preview
@Composable
private fun SmsOtpErrorBottomSheetContentPreview() {
    DashlaneModalBottomSheetPreview {
        SmsOtpErrorBottomSheetContent(
            message = stringResource(R.string.autofill_sms_otp_dialog_error_detail_timeout),
            onCancelClick = {},
        )
    }
}