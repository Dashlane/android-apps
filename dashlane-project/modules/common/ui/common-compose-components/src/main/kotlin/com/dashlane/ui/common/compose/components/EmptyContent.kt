package com.dashlane.ui.common.compose.components

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.dashlane.design.component.ExpressiveIconSize
import com.dashlane.design.iconography.IconToken
import com.dashlane.design.iconography.IconTokens
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.color.Mood
import com.dashlane.design.theme.tooling.DashlanePreview

@Composable
fun EmptyContent(
    iconToken: IconToken,
    title: String,
    description: String?,
    modifier: Modifier = Modifier,
    extraContent: (@Composable ColumnScope.() -> Unit)? = null,
) {
    SimpleStateContent(
        modifier = modifier,
        iconToken = iconToken,
        iconMood = Mood.Neutral,
        iconSize = ExpressiveIconSize.Large,
        title = title,
        titleStyle = DashlaneTheme.typography.titleSectionMedium,
        description = description,
        descriptionColor = DashlaneTheme.colors.textNeutralQuiet,
        extraContent = extraContent,
    )
}

@Composable
fun BaseEmptyContent(
    iconToken: IconToken,
    title: String,
    description: String?,
    modifier: Modifier = Modifier,
) {
    BaseSimpleStateContent(
        modifier = modifier,
        iconToken = iconToken,
        iconMood = Mood.Neutral,
        iconSize = ExpressiveIconSize.Large,
        title = title,
        titleStyle = DashlaneTheme.typography.titleSectionMedium,
        description = description,
        descriptionColor = DashlaneTheme.colors.textNeutralQuiet,
    )
}

@Preview
@Composable
private fun VaultListEmptyPreview() = DashlanePreview {
    EmptyContent(
        iconToken = IconTokens.protectionOutlined,
        title = "One secure place for all your passwords, personal info, and payment details",
        description = "Add anything you want to keep safe and Dashlane will keep it secure and easy to access.",
    )
}