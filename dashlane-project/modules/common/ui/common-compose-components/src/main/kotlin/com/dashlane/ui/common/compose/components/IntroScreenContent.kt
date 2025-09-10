package com.dashlane.ui.common.compose.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dashlane.design.component.ButtonLarge
import com.dashlane.design.component.ButtonLayout
import com.dashlane.design.component.ExpressiveIcon
import com.dashlane.design.component.ExpressiveIconSize
import com.dashlane.design.component.Text
import com.dashlane.design.component.cardBackground
import com.dashlane.design.iconography.IconToken
import com.dashlane.design.iconography.IconTokens
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.color.Intensity
import com.dashlane.design.theme.color.Mood
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.ui.common.compose.components.screentemplate.FillAndCenterContent

object IntroScreenContent {
    data class Item(
        val icon: IconToken,
        val title: String,
        val description: String,
    )

    data class Action(
        val layout: ButtonLayout,
        val onClick: () -> Unit,
    ) {
        constructor(text: String, onClick: () -> Unit) : this(
            layout = ButtonLayout.TextOnly(text = text),
            onClick = onClick,
        )
    }
}

@Composable
fun IntroScreenContent(
    iconToken: IconToken,
    title: String,
    items: List<IntroScreenContent.Item>,
    action: IntroScreenContent.Action?,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(24.dp),
) {
    FillAndCenterContent(
        modifier = modifier,
        contentPadding = contentPadding,
    ) {
        BaseIntroScreenContent(
            iconToken = iconToken,
            title = title,
            items = items,
            action = action,
        )
    }
}

@Composable
fun BaseIntroScreenContent(
    iconToken: IconToken,
    title: String,
    items: List<IntroScreenContent.Item>,
    action: IntroScreenContent.Action?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ExpressiveIcon(
            icon = iconToken,
            size = ExpressiveIconSize.XLarge,
            mood = Mood.Brand,
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            modifier = Modifier.semantics { heading() },
            text = title,
            textAlign = TextAlign.Center,
            style = DashlaneTheme.typography.titleSectionLarge,
            color = DashlaneTheme.colors.textNeutralCatchy,
        )
        if (items.isNotEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))
            items.forEachIndexed { index, item ->
                val isFirstItem = index == 0
                val isLastItem = index == items.size - 1
                Item(
                    modifier = Modifier
                        .fillMaxWidth()
                        .cardBackground(
                            isTop = isFirstItem,
                            isBottom = isLastItem,
                        )
                        .padding(
                            start = 16.dp,
                            end = 16.dp,
                            top = if (isFirstItem) 16.dp else 0.dp,
                            bottom = 16.dp,
                        ),
                    item = item,
                )
            }
        }
        if (action != null) {
            Spacer(modifier = Modifier.height(24.dp))
            ButtonLarge(
                layout = action.layout,
                onClick = action.onClick,
                mood = Mood.Brand,
                intensity = Intensity.Catchy,
            )
        }
    }
}

@Composable
private fun Item(
    item: IntroScreenContent.Item,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .semantics(mergeDescendants = true) {},
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ExpressiveIcon(
            icon = item.icon,
            size = ExpressiveIconSize.Medium,
            mood = Mood.Neutral,
        )
        Spacer(
            modifier = Modifier.width(16.dp),
        )
        Column {
            Text(
                text = item.title,
                style = DashlaneTheme.typography.titleBlockMedium,
                color = DashlaneTheme.colors.textNeutralCatchy,
            )
            Spacer(
                modifier = Modifier.height(4.dp),
            )
            Text(
                text = item.description,
                style = DashlaneTheme.typography.bodyReducedRegular,
                color = DashlaneTheme.colors.textNeutralQuiet,
            )
        }
    }
}

@Preview
@Composable
private fun IntroScreenContentPreview() {
    DashlanePreview {
        IntroScreenContent(
            iconToken = IconTokens.featurePasswordHealthOutlined,
            title = "Check your Password Health score",
            items = listOf(
                IntroScreenContent.Item(
                    icon = IconTokens.itemLoginOutlined,
                    title = "Find weak spots",
                    description = "Update simple or reused passwords.",
                ),
                IntroScreenContent.Item(
                    icon = IconTokens.tipOutlined,
                    title = "Stay safe",
                    description = "Replace breached passwords quickly.",
                ),
                IntroScreenContent.Item(
                    icon = IconTokens.healthPositiveOutlined,
                    title = "Strengthen security",
                    description = "Strive for a higher score for better protection.",
                ),
            ),
            action = IntroScreenContent.Action(
                layout = ButtonLayout.TextOnly("Add login"),
                onClick = {},
            ),
        )
    }
}