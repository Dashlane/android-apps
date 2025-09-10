package com.dashlane.autofill.securitywarnings.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.dashlane.autofill.api.R
import com.dashlane.design.component.BottomSheetActions
import com.dashlane.design.component.BottomSheetContent
import com.dashlane.design.component.BottomSheetDefaults
import com.dashlane.design.component.ModalBottomSheet
import com.dashlane.design.component.tooling.DashlaneModalBottomSheetPreview
import com.dashlane.design.iconography.IconTokens
import com.dashlane.design.theme.color.Mood
import com.dashlane.ui.common.compose.components.StatusBarBackground

@Composable
internal fun UnknownSourceWarningBottomSheetScreen(
    onAllowClick: () -> Unit,
    onCancelClick: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    Box {
        StatusBarBackground()
        UnknownSourceWarningBottomSheet(
            onAllowClick = onAllowClick,
            onCancelClick = onCancelClick,
            onDismissRequest = onDismissRequest,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UnknownSourceWarningBottomSheet(
    onAllowClick: () -> Unit,
    onCancelClick: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    ModalBottomSheet(
        sheetState = rememberModalBottomSheetState(),
        onDismissRequest = onDismissRequest,
    ) {
        UnknownSourceWarningBottomSheetContent(
            onAllowClick = onAllowClick,
            onCancelClick = onCancelClick,
        )
    }
}

@Composable
private fun UnknownSourceWarningBottomSheetContent(
    onAllowClick: () -> Unit,
    onCancelClick: () -> Unit,
) {
    BottomSheetContent(
        decoration = BottomSheetDefaults.decoration(
            iconToken = IconTokens.feedbackWarningOutlined,
            mood = Mood.Warning,
        ),
        title = stringResource(R.string.autofill_unknown_warning_title),
        description = stringResource(R.string.autofill_unknown_warning_message),
        actions = BottomSheetActions(
            primaryButtonText = stringResource(R.string.autofill_unknown_warning_accept),
            onPrimaryButtonClick = onAllowClick,
            secondaryButtonText = stringResource(R.string.autofill_warning_cancel),
            onSecondaryButtonClick = onCancelClick,
        ),
    )
}

@Preview
@Composable
private fun UnknownSourceWarningBottomSheetContentPreview() {
    DashlaneModalBottomSheetPreview {
        UnknownSourceWarningBottomSheetContent(
            onAllowClick = {},
            onCancelClick = {},
        )
    }
}