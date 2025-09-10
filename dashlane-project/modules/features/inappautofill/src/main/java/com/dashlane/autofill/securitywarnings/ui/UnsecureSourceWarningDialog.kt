package com.dashlane.autofill.securitywarnings.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dashlane.autofill.api.R
import com.dashlane.autofill.formdetector.model.AutoFillFormSource
import com.dashlane.autofill.securitywarnings.UnsecureSourceState
import com.dashlane.autofill.securitywarnings.UnsecureSourceViewModel
import com.dashlane.design.component.ButtonLayout
import com.dashlane.design.component.Dialog
import com.dashlane.design.component.Text
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.ui.common.compose.components.CheckboxWithLabel
import com.dashlane.ui.common.compose.components.StatusBarBackground

@Composable
internal fun UnsecureSourceWarningDialog(
    viewModel: UnsecureSourceViewModel,
    onFillCancelled: (AutoFillFormSource) -> Unit,
    onFillAllowed: (AutoFillFormSource, Boolean) -> Unit,
    onFillDenied: (AutoFillFormSource) -> Unit,
) {
    LaunchedEffect(viewModel) {
        viewModel.stateFlow.sideEffect.collect { sideEffect ->
            when (sideEffect) {
                is UnsecureSourceState.SideEffect.OnFillAllowed ->
                    onFillAllowed(sideEffect.formSource, sideEffect.doNotShowAgainChecked)

                is UnsecureSourceState.SideEffect.OnFillCancelled ->
                    onFillCancelled(sideEffect.formSource)

                is UnsecureSourceState.SideEffect.OnFillDenied ->
                    onFillDenied(sideEffect.formSource)
            }
        }
    }

    val uiState by viewModel.stateFlow.viewState.collectAsStateWithLifecycle()
    Box {
        StatusBarBackground()
        UnsecureSourceWarningDialog(
            doNotShowAgainChecked = uiState.doNotShowAgainChecked,
            onDoNotShowAgainToggleChange = viewModel::onDoNotShowAgainToggleChange,
            onDismiss = viewModel::onFillCancelled,
            onAllowClick = viewModel::onFillAllowed,
            onCancelClick = viewModel::onFillDenied,
        )
    }
}

@Composable
private fun UnsecureSourceWarningDialog(
    doNotShowAgainChecked: Boolean,
    onDoNotShowAgainToggleChange: () -> Unit,
    onDismiss: () -> Unit,
    onAllowClick: (Boolean) -> Unit,
    onCancelClick: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        title = stringResource(R.string.autofill_incorrect_warning_title),
        description = {
            Text(
                text = stringResource(R.string.autofill_incorrect_warning_message),
            )
            Spacer(
                modifier = Modifier.height(16.dp),
            )
            CheckboxWithLabel(
                modifier = Modifier.fillMaxWidth(),
                label = stringResource(R.string.autofill_warning_do_not_show_again),
                checked = doNotShowAgainChecked,
                onClick = onDoNotShowAgainToggleChange,
            )
        },
        mainActionLayout = ButtonLayout.TextOnly(stringResource(R.string.autofill_warning_accept)),
        mainActionClick = { onAllowClick(doNotShowAgainChecked) },
        additionalActionLayout = ButtonLayout.TextOnly(stringResource(R.string.autofill_warning_cancel)),
        additionalActionClick = onCancelClick,
    )
}

@Preview
@Composable
private fun UnsecureSourceWarningDialogPreview() {
    DashlanePreview {
        var doNotShowAgainChecked by remember { mutableStateOf(false) }
        UnsecureSourceWarningDialog(
            doNotShowAgainChecked = doNotShowAgainChecked,
            onDoNotShowAgainToggleChange = { doNotShowAgainChecked = !doNotShowAgainChecked },
            onDismiss = {},
            onAllowClick = {},
            onCancelClick = {},
        )
    }
}