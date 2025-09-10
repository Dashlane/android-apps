package com.dashlane.m2w.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.m2w.R
import com.dashlane.ui.common.compose.components.basescreen.BoardingScreen
import com.dashlane.ui.common.compose.components.basescreen.BoardingScreenDefaults

@Composable
internal fun M2wIntroScreen(
    onStartClick: () -> Unit,
    onSkipClick: () -> Unit,
) {
    BoardingScreen(
        decoration = {
            Image(
                modifier = Modifier.height(96.dp),
                painter = painterResource(id = R.drawable.logo_m2w),
                contentDescription = null,
            )
        },
        title = stringResource(R.string.m2w_intro_title),
        description = stringResource(R.string.m2w_intro_body),
        actions = BoardingScreenDefaults.Actions(
            primaryButtonText = stringResource(R.string.m2w_intro_start),
            onPrimaryButtonClick = onStartClick,
            secondaryButtonText = stringResource(R.string.m2w_intro_skip),
            onSecondaryButtonClick = onSkipClick,
        ),
    )
}

@Preview
@Composable
private fun M2wIntroScreenPreview() {
    DashlanePreview {
        M2wIntroScreen(
            onStartClick = {},
            onSkipClick = {},
        )
    }
}