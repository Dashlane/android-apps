package com.dashlane.util.animation

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.repeatable
import androidx.compose.animation.core.tween
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer

fun Modifier.shake(enabled: Boolean) = composed(
    factory = {
        val scale by animateFloatAsState(
            targetValue = if (enabled) 10f else 0f,
            animationSpec = repeatable(
                iterations = 7,
                animation = tween(durationMillis = 50, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "shakeAnimation"
        )
        Modifier.graphicsLayer {
            translationX = if (enabled) scale else 0f
        }
    }
)