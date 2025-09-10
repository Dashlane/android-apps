package com.dashlane.premium.current.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.dashlane.design.component.BottomSheetActions
import com.dashlane.design.component.BottomSheetContent
import com.dashlane.design.component.ModalBottomSheet
import com.dashlane.design.component.tooling.DashlaneModalBottomSheetPreview
import com.dashlane.premium.ui.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DarkWebMonitoringBottomSheet(
    onDismissRequest: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
    ) {
        DarkWebMonitoringBottomSheetContent()
    }
}

@Composable
private fun DarkWebMonitoringBottomSheetContent() {
    BottomSheetContent(
        decoration = null,
        title = stringResource(R.string.current_benefit_more_info_dark_web_monitoring_title),
        description = stringResource(R.string.current_benefit_more_info_dark_web_monitoring_text),
        actions = BottomSheetActions(
            primaryButtonText = null,
            onPrimaryButtonClick = null,
            secondaryButtonText = null,
            onSecondaryButtonClick = null,
        ),
    )
}

@Preview
@Composable
private fun DarkWebMonitoringBottomSheetContentPreview() {
    DashlaneModalBottomSheetPreview {
        DarkWebMonitoringBottomSheetContent()
    }
}