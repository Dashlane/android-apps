package com.dashlane.ui.widgets.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.airbnb.lottie.compose.rememberLottieDynamicProperties
import com.airbnb.lottie.compose.rememberLottieDynamicProperty
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.ui.R

@Composable
fun DashlaneLoading(
    modifier: Modifier = Modifier
) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.lottie_loading_indeterminate))
    val load = DashlaneTheme.colors.borderNeutralQuietIdle.toArgb()
    val load2 = DashlaneTheme.colors.textInverseStandard.value.toArgb()
    val load3 = DashlaneTheme.colors.textPositiveQuiet.value.toArgb()
    val load4 = DashlaneTheme.colors.textDangerQuiet.value.toArgb()
    val layer1CopyOutlines = DashlaneTheme.colors.textPositiveQuiet.value.toArgb()
    val layer1Outlines = DashlaneTheme.colors.textDangerQuiet.value.toArgb()

    val dynamicProperties = rememberLottieDynamicProperties(
        rememberLottieDynamicProperty(
            keyPath = arrayOf("load", "**"),
            property = LottieProperty.STROKE_COLOR,
            callback = { load }
        ),
        rememberLottieDynamicProperty(
            keyPath = arrayOf("load 2", "**"),
            property = LottieProperty.STROKE_COLOR,
            callback = { load2 }
        ),
        rememberLottieDynamicProperty(
            keyPath = arrayOf("load 3", "**"),
            property = LottieProperty.STROKE_COLOR,
            callback = { load3 }
        ),
        rememberLottieDynamicProperty(
            keyPath = arrayOf("load 4", "**"),
            property = LottieProperty.STROKE_COLOR,
            callback = { load4 }
        ),
        rememberLottieDynamicProperty(
            keyPath = arrayOf("Layer 1 copy Outlines", "**"),
            property = LottieProperty.COLOR,
            callback = { layer1CopyOutlines }
        ),
        rememberLottieDynamicProperty(
            keyPath = arrayOf("Layer 1 Outlines", "**"),
            property = LottieProperty.COLOR,
            callback = { layer1Outlines }
        )
    )

    LottieAnimation(
        modifier = modifier,
        iterations = LottieConstants.IterateForever,
        composition = composition,
        dynamicProperties = dynamicProperties
    )
}
