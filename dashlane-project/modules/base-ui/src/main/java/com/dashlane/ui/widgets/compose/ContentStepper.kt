package com.dashlane.ui.widgets.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.dashlane.design.component.Text
import com.dashlane.design.theme.DashlaneTheme

@Composable
fun ContentStepper(
    modifier: Modifier = Modifier,
    content: List<AnnotatedString>
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                DashlaneTheme.colors.containerAgnosticNeutralSupershy,
                RoundedCornerShape(10.dp)
            )
            .padding(24.dp)
    ) {
        content.forEachIndexed { index, string ->
            val step = index + 1
            val bottomPadding = if (step == content.size) 0.dp else 16.dp
            Row(modifier = Modifier.padding(bottom = bottomPadding)) {
                ContentStepCircle(
                    text = step.toString(),
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
                Text(
                    text = string,
                    style = DashlaneTheme.typography.bodyStandardRegular,
                    color = DashlaneTheme.colors.textNeutralCatchy,
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .padding(start = 12.dp)
                )
            }
        }
    }
}

@Composable
private fun ContentStepCircle(
    text: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(ContentStepCircleSize)
            .background(DashlaneTheme.colors.containerExpressiveBrandQuietIdle, CircleShape)
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            modifier = Modifier
                .width(ContentStepCircleSize)
                .heightIn(max = ContentStepCircleSize),
            text = text,
            style = DashlaneTheme.typography.bodyStandardRegular,
            color = DashlaneTheme.colors.textBrandStandard,
            textAlign = TextAlign.Center,
            overflow = TextOverflow.Visible
        )
    }
}

private val ContentStepCircleSize = 32.dp
