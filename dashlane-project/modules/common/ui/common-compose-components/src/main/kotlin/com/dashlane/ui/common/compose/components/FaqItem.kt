package com.dashlane.ui.common.compose.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dashlane.design.component.HtmlText
import com.dashlane.design.component.Text
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.tooling.DashlanePreview

@Composable
fun FaqItem(
    question: String,
    answer: String,
) {
    Text(
        modifier = Modifier.semantics { heading() },
        text = question,
        color = DashlaneTheme.colors.textNeutralCatchy,
        style = DashlaneTheme.typography.titleBlockMedium,
    )
    Spacer(
        modifier = Modifier.height(8.dp),
    )
    HtmlText(
        htmlText = answer,
        color = DashlaneTheme.colors.textNeutralStandard,
        style = DashlaneTheme.typography.bodyStandardRegular,
    )
}

object FaqDefaults {
    val verticalSpacing = 32.dp
}

@Preview
@Composable
private fun FaqItemPreview() {
    DashlanePreview {
        Column {
            FaqItem(
                question = "Question 1",
                answer = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod.",
            )
            Spacer(
                modifier = Modifier.height(FaqDefaults.verticalSpacing),
            )
            FaqItem(
                question = "Question 2",
                answer = "Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris.",
            )
        }
    }
}