package com.dashlane.ui.widgets.compose

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.dashlane.design.component.Text
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.ui.R

@Composable
fun DashlaneLoading(
    modifier: Modifier = Modifier,
    hasFinishedLoading: Boolean,
    verticalArrangement: Arrangement.Vertical,
    horizontalAlignment: Alignment.Horizontal,
    successText: String? = null
) {
    val success by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.lottie_loading_success))

    Column(
        modifier = modifier,
        verticalArrangement = verticalArrangement,
        horizontalAlignment = horizontalAlignment
    ) {
        if (hasFinishedLoading) {
            LottieAnimation(
                modifier = Modifier
                    .size(120.dp),
                composition = success,
                iterations = 1
            )

            successText?.let {
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(
                        
                        initialAlpha = 0.3f
                    )
                ) {
                    Text(
                        text = successText,
                        style = DashlaneTheme.typography.titleSectionLarge,
                        color = DashlaneTheme.colors.textNeutralCatchy,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .padding(bottom = 32.dp)
                    )
                }
            }
        } else {
            IndeterminateLoading(
                modifier = Modifier
                    .size(120.dp),
            )
        }
    }
}

@Composable
fun IndeterminateLoading(
    modifier: Modifier = Modifier
) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.lottie_loading_indeterminate))
    LottieAnimation(
        modifier = modifier,
        iterations = LottieConstants.IterateForever,
        composition = composition
    )
}

@Preview
@Composable
fun DashlaneLoadingPreview() {
    DashlanePreview {
        DashlaneLoading(
            modifier = Modifier,
            hasFinishedLoading = false,
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            successText = "Success"
        )
    }
}
