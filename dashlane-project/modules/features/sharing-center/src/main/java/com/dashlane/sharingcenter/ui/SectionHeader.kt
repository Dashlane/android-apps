package com.dashlane.sharingcenter.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dashlane.design.component.Text
import com.dashlane.design.component.cardBackground
import com.dashlane.design.theme.DashlaneTheme

@Composable
internal fun SectionHeader(title: String, modifier: Modifier = Modifier) {
    Text(
        modifier = modifier
            .fillMaxWidth()
            .cardBackground(isTop = true, isBottom = false)
            .padding(
                top = 16.dp,
                bottom = 8.dp,
                start = 16.dp,
                end = 16.dp,
            ),
        text = title,
        style = DashlaneTheme.typography.titleBlockMedium,
        color = DashlaneTheme.colors.textNeutralCatchy,
    )
}