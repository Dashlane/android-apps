package com.dashlane.ui.common.compose.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dashlane.design.component.Icon
import com.dashlane.design.component.Text
import com.dashlane.design.iconography.IconToken
import com.dashlane.design.iconography.IconTokens
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.tooling.DashlanePreview

@Composable
fun EmptyScreen(
    modifier: Modifier = Modifier,
    icon: IconToken,
    title: String,
    description: String,
) {
    Column(
        modifier = modifier
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            modifier = Modifier.size(96.dp),
            token = icon,
            tint = DashlaneTheme.colors.textBrandQuiet,
            contentDescription = null,
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = title,
            textAlign = TextAlign.Center,
            style = DashlaneTheme.typography.titleSectionLarge,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = description,
            textAlign = TextAlign.Center,
            style = DashlaneTheme.typography.bodyStandardRegular,
        )
    }
}

@Preview
@Composable
private fun VaultListEmptyPreview(modifier: Modifier = Modifier) = DashlanePreview {
    EmptyScreen(
        icon = IconTokens.protectionOutlined,
        title = "One secure place for all your passwords, personal info, and payment details",
        description = "Add anything you want to keep safe and Dashlane will keep it secure and easy to access.",
    )
}