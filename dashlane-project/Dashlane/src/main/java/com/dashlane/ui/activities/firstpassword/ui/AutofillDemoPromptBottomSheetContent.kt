package com.dashlane.ui.activities.firstpassword.ui

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.dashlane.R
import com.dashlane.design.component.BottomSheetActions
import com.dashlane.design.component.BottomSheetContent
import com.dashlane.design.component.tooling.DashlaneModalBottomSheetPreview

@Composable
internal fun AutofillDemoPromptBottomSheetContent(
    onTryDemoClick: () -> Unit,
    onReturnHomeClick: () -> Unit,
) {
    BottomSheetContent(
        title = stringResource(R.string.autofill_demo_bottom_sheet_title),
        description = stringResource(R.string.autofill_demo_bottom_sheet_subtitle),
        extraContent = {
            val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.lottie_welcome_autofill))
            LottieAnimation(
                modifier = Modifier
                    .size(150.dp)
                    .align(Alignment.CenterHorizontally),
                iterations = LottieConstants.IterateForever,
                composition = composition,
            )
        },
        actions = BottomSheetActions(
            primaryButtonText = stringResource(R.string.autofill_demo_bottom_sheet_try_demo),
            onPrimaryButtonClick = onTryDemoClick,
            secondaryButtonText = stringResource(R.string.autofill_demo_bottom_sheet_return_home),
            onSecondaryButtonClick = onReturnHomeClick,
        ),
    )
}

@Preview
@Composable
private fun AutofillDemoPromptBottomSheetContentPreview() {
    DashlaneModalBottomSheetPreview {
        AutofillDemoPromptBottomSheetContent(
            onTryDemoClick = {},
            onReturnHomeClick = {},
        )
    }
}