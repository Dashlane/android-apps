package com.dashlane.ui.widgets.compose

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.util.font.WalsheimProBold

@OptIn(ExperimentalTextApi::class)
@Composable
fun OutlinedTeamspaceIcon(
    letter: Char,
    color: Int,
    iconSize: Dp,
    modifier: Modifier = Modifier,
    teamspaceDescription: String? = null
) {
    val textMeasurer = rememberTextMeasurer()

    Spacer(
        modifier = modifier
            .size(iconSize)
            .semantics {
                teamspaceDescription?.let {
                    contentDescription = teamspaceDescription
                }
            }
            .scale(0.75f) 
            
            .drawWithCache {
                var fontSize = 30.sp
                lateinit var measuredText: TextLayoutResult

                while (
                    run {
                        measuredText = textMeasurer.measure(
                            text = letter.toString(),
                            style = TextStyle(
                                color = Color(color),
                                fontSize = fontSize,
                                fontFamily = WalsheimProBold.fontFamily,
                                fontWeight = FontWeight(weight = 600),
                                lineHeightStyle = LineHeightStyle(
                                    alignment = LineHeightStyle.Alignment.Center,
                                    trim = LineHeightStyle.Trim.None
                                )
                            )
                        )
                        measuredText.size.height
                    } > size.height * 0.8f
                ) {
                    fontSize *= 0.9f
                }

                onDrawBehind {
                    drawPath(
                        path = buildOutlinedPath(size.width, size.height),
                        color = Color(color),
                        style = Fill
                    )

                    drawText(
                        textLayoutResult = measuredText,
                        topLeft = Offset(
                            x = (size.width - measuredText.size.width) * 0.5f,
                            y = (size.height - measuredText.size.height) * 0.5f
                        )
                    )
                }
            }
    )
}

private fun buildOutlinedPath(width: Float, height: Float): Path = Path().apply {
    
    val a = minOf(width, height) / 72f

    moveTo(a * 33f, a * 64f)
    lineTo(a * 8f, a * 64f)
    lineTo(a * 8f, a * 28f)
    lineTo(a * 28f, a * 8f)
    lineTo(a * 64f, a * 8f)
    lineTo(a * 64f, a * 44f)
    lineTo(a * 44f, a * 64f)
    lineTo(a * 44f, a * 72f)
    lineTo(a * 48f, a * 72f)
    lineTo(a * 72f, a * 48f)
    lineTo(a * 72f, a * 0f)
    lineTo(a * 24f, a * 0f)
    lineTo(a * 0f, a * 24f)
    lineTo(a * 0f, a * 72f)
    lineTo(a * 37f, a * 72f)
    close()
}

@Preview
@Composable
fun TeamspaceIconPreview() {
    DashlanePreview {
        OutlinedTeamspaceIcon(
            letter = 'E',
            color = Color.Magenta.toArgb(),
            iconSize = 12.dp
        )
    }
}

@Preview
@Composable
fun TeamspaceIconLargePreview() {
    DashlanePreview {
        OutlinedTeamspaceIcon(
            letter = 'E',
            color = Color.Magenta.toArgb(),
            iconSize = 72.dp
        )
    }
}