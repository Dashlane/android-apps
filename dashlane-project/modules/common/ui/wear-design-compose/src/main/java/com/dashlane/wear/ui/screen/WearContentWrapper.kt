package com.dashlane.wear.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.ui.tooling.preview.WearPreviewSmallRound
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.wear.ui.DashlaneWearPreview
import com.dashlane.wear.ui.backgroundDefaultWear
import com.dashlane.wear.ui.component.DashlaneLogo
import com.dashlane.wear.ui.component.WearText
import com.dashlane.wear.ui.utils.scrollAway

@Composable
fun WearContentWrapper(
    modifier: Modifier = Modifier,
    isTimeTextVisible: Boolean = true,
    isLogoVisible: Boolean = false,
    scrollableState: ScrollableState? = null,
    vignette: @Composable (() -> Unit)? = null,
    positionIndicator: @Composable (() -> Unit)? = null,
    pageIndicator: @Composable (() -> Unit)? = null,
    content: @Composable (PaddingValues) -> Unit,
) {
    val timeTextPadding =
        remember(isTimeTextVisible) { timeTextPadding.takeIf { isTimeTextVisible } ?: 0.dp }
    val logoPadding =
        remember(isLogoVisible) { dashlaneLogoPadding.takeIf { isLogoVisible } ?: 0.dp }
    val contentTopPadding =
        remember(isTimeTextVisible || isLogoVisible) { timeTextPadding + logoPadding }

    Scaffold(
        modifier = modifier
            .background(color = DashlaneTheme.colors.backgroundDefaultWear),
        vignette = vignette,
        positionIndicator = positionIndicator,
        pageIndicator = pageIndicator,
        timeText = {
            if (isTimeTextVisible) {
                TimeText(
                    modifier = Modifier.scrollAway(
                        scrollableState = scrollableState,
                        itemIndex = 0,
                        offset = contentTopPadding,
                    ),
                )
            }
        },
    ) {
        
        AnimatedVisibility(
            visible = isLogoVisible && (scrollableState?.canScrollBackward != true),
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(timeTextPadding.takeUnless { it == 0.dp } ?: 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                DashlaneLogo()
            }
        }

        content(PaddingValues(top = contentTopPadding))
    }
}

@Composable
@WearPreviewSmallRound
private fun WearContentWrapperPreview() {
    DashlaneWearPreview {
        WearContentWrapper(
            modifier = Modifier.fillMaxSize(),
            isLogoVisible = true,
        ) { contentPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                WearText(text = "Hello Dashlane!")
                WearText(text = "How do you do?")
            }
        }
    }
}

private val timeTextPadding = 32.dp
private val dashlaneLogoPadding = 24.dp