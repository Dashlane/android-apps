package com.dashlane.limitations

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dashlane.design.component.BottomSheetActions
import com.dashlane.design.component.BottomSheetContent
import com.dashlane.design.component.BottomSheetDefaults
import com.dashlane.design.component.tooling.DashlaneModalBottomSheetPreview
import com.dashlane.design.iconography.IconTokens
import com.dashlane.design.theme.color.Mood
import com.dashlane.ui.R

@Composable
internal fun PasswordLimitBottomSheetScreen(
    onUpgradeClicked: () -> Unit,
    onNotNowClicked: () -> Unit,
) {
    PasswordLimitBottomSheetContent(
        modifier = Modifier
            .background(BottomSheetDefaults.background, BottomSheetDefaults.shape)
            .padding(top = 24.dp),
        onUpgradeClicked = onUpgradeClicked,
        onNotNowClicked = onNotNowClicked,
    )
}

@Composable
internal fun PasswordLimitBottomSheetContent(
    modifier: Modifier = Modifier,
    onUpgradeClicked: () -> Unit,
    onNotNowClicked: () -> Unit,
) {
    BottomSheetContent(
        modifier = modifier,
        decoration = BottomSheetDefaults.decoration(IconTokens.premiumOutlined, Mood.Brand),
        title = stringResource(id = R.string.password_limit_bottom_sheet_title),
        description = stringResource(id = R.string.password_limit_bottom_sheet_message),
        actions = BottomSheetActions(
            primaryButtonText = stringResource(id = R.string.password_limit_bottom_sheet_upgrade),
            onPrimaryButtonClick = onUpgradeClicked,
            secondaryButtonText = stringResource(id = R.string.password_limit_bottom_sheet_cancel),
            onSecondaryButtonClick = onNotNowClicked,
        ),
    )
}

@Preview
@Composable
private fun PasswordLimitBottomSheetPreview() {
    DashlaneModalBottomSheetPreview {
        PasswordLimitBottomSheetContent(
            onUpgradeClicked = {},
            onNotNowClicked = {},
        )
    }
}