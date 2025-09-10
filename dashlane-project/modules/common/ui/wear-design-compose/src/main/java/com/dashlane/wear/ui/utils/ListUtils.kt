package com.dashlane.wear.ui.utils

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyListState
import androidx.wear.compose.material.scrollAway

fun Modifier.Companion.scrollAway(
    scrollableState: ScrollableState?,
    itemIndex: Int = 1,
    offset: Dp = 0.dp,
): Modifier {
    return when (scrollableState) {
        is ScrollState -> scrollAway(scrollState = scrollableState, offset = offset)
        is LazyListState -> scrollAway(
            scrollState = scrollableState,
            itemIndex = itemIndex,
            offset = offset,
        )
        is ScalingLazyListState -> scrollAway(scrollState = scrollableState, itemIndex = itemIndex, offset = offset)
        else -> this
    }
}