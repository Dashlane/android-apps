package com.dashlane.ui.common.compose.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dashlane.design.component.ExpressiveIcon
import com.dashlane.design.component.ExpressiveIconSize
import com.dashlane.design.component.Text
import com.dashlane.design.iconography.IconToken
import com.dashlane.design.iconography.IconTokens
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.color.Mood
import com.dashlane.design.theme.color.TextColor
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.ui.common.compose.components.screentemplate.FillAndCenterContent

@SuppressWarnings("kotlin:S107") 
@Composable
fun SimpleStateContent(
    iconToken: IconToken,
    iconMood: Mood,
    title: String,
    description: String?,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(24.dp),
    iconSize: ExpressiveIconSize = ExpressiveIconSize.XLarge,
    titleStyle: TextStyle = DashlaneTheme.typography.titleSectionLarge,
    descriptionColor: TextColor = DashlaneTheme.colors.textNeutralStandard,
    extraContent: (@Composable ColumnScope.() -> Unit)? = null,
) {
    FillAndCenterContent(
        modifier = modifier,
        contentPadding = contentPadding,
    ) {
        BaseSimpleStateContent(
            iconToken = iconToken,
            iconMood = iconMood,
            title = title,
            description = description,
            iconSize = iconSize,
            titleStyle = titleStyle,
            descriptionColor = descriptionColor,
        )

        extraContent?.invoke(this)
    }
}

@SuppressWarnings("kotlin:S107") 
@Composable
fun BaseSimpleStateContent(
    iconToken: IconToken,
    iconMood: Mood,
    title: String,
    description: String?,
    modifier: Modifier = Modifier,
    iconSize: ExpressiveIconSize = ExpressiveIconSize.XLarge,
    titleStyle: TextStyle = DashlaneTheme.typography.titleSectionLarge,
    descriptionColor: TextColor = DashlaneTheme.colors.textNeutralStandard,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ExpressiveIcon(
            icon = iconToken,
            size = iconSize,
            mood = iconMood,
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            modifier = Modifier.semantics { heading() },
            text = title,
            textAlign = TextAlign.Center,
            style = titleStyle,
            color = DashlaneTheme.colors.textNeutralCatchy,
        )

        if (description != null) {
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = description,
                textAlign = TextAlign.Center,
                style = DashlaneTheme.typography.bodyStandardRegular,
                color = descriptionColor,
            )
        }
    }
}

@Preview
@Composable
private fun SimpleStateContentPreview() {
    DashlanePreview {
        SimpleStateContent(
            iconToken = IconTokens.itemDriversLicenseOutlined,
            iconMood = Mood.Positive,
            title = "Title",
            description = "Description",
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(16.dp)
                    .background(DashlaneTheme.colors.containerDecorativeBlue),
            )
        }
    }
}