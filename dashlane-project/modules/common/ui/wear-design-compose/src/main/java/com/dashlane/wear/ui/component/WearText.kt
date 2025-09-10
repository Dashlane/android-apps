package com.dashlane.wear.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.dashlane.design.component.Text
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.color.TextColor

@Composable
fun WearText(
    text: String,
    modifier: Modifier = Modifier,
    textAlign: TextAlign? = null,
    maxLines: Int = 2,
    overflow: TextOverflow = TextOverflow.Ellipsis,
    color: TextColor = DashlaneTheme.colors.textInverseCatchy,
    style: TextStyle = DashlaneTheme.typography.bodyStandardRegular,
) {
    Text(
        modifier = modifier,
        textAlign = textAlign,
        maxLines = maxLines,
        overflow = overflow,
        text = text,
        color = color,
        style = style,
    )
}