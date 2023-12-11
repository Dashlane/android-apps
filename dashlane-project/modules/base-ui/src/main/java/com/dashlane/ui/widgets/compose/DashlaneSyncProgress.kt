package com.dashlane.ui.widgets.compose

import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.airbnb.lottie.compose.rememberLottieDynamicProperties
import com.airbnb.lottie.compose.rememberLottieDynamicProperty
import com.dashlane.design.component.Text
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.ui.R

@Composable
fun DashlaneSyncProgress(
    modifier: Modifier = Modifier,
    color: Color,
    hasFinishedLoading: Boolean,
    verticalArrangement: Arrangement.Vertical,
    horizontalAlignment: Alignment.Horizontal,
    progress: Int?,
    progressText: String,
    successText: String
) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.lottie_sync_progress))
    val dynamicProperties = rememberLottieDynamicProperties(
        rememberLottieDynamicProperty(
            keyPath = arrayOf("**"),
            property = LottieProperty.COLOR_FILTER,
            callback = { PorterDuffColorFilter(color.toArgb(), PorterDuff.Mode.SRC_ATOP) }
        )
    )

    val currentProgress: Int by remember(progress) { mutableStateOf(progress ?: 0) }
    val animatedProgress by animateIntAsState(
        targetValue = currentProgress,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "progressAnimation"
    )

    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = verticalArrangement,
        horizontalAlignment = horizontalAlignment
    ) {
        var isTextVisible by remember { mutableStateOf(false) }

        LaunchedEffect(hasFinishedLoading) {
            if (hasFinishedLoading) {
                isTextVisible = true
            }
        }

        LottieAnimation(
            modifier = Modifier.size(96.dp),
            iterations = LottieConstants.IterateForever,
            composition = composition,
            dynamicProperties = dynamicProperties
        )

        Text(
            text = "$animatedProgress %",
            style = DashlaneTheme.typography.bodyStandardRegular,
            color = DashlaneTheme.colors.textNeutralStandard,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(top = 56.dp)
        )

        Text(
            text = if (hasFinishedLoading) successText else progressText,
            style = DashlaneTheme.typography.titleSectionLarge,
            color = DashlaneTheme.colors.textNeutralCatchy,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(top = 40.dp)
                .padding(bottom = 32.dp)
        )
    }
}

@Preview
@Composable
fun DashlaneSyncProgressPreview() {
    DashlanePreview {
        DashlaneSyncProgress(
            modifier = Modifier,
            color = DashlaneTheme.colors.oddityBrand,
            hasFinishedLoading = false,
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            progress = 75,
            progressText = "progress",
            successText = "Success"
        )
    }
}