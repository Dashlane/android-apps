package com.dashlane.ui.widgets.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dashlane.design.component.ButtonLayout
import com.dashlane.design.component.ButtonMedium
import com.dashlane.design.component.ButtonMediumBar
import com.dashlane.design.component.Text
import com.dashlane.design.component.Thumbnail
import com.dashlane.design.component.ThumbnailSize
import com.dashlane.design.component.ThumbnailType
import com.dashlane.design.iconography.IconToken
import com.dashlane.design.iconography.IconTokens
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.color.Intensity
import com.dashlane.design.theme.tooling.DashlanePreview

@Composable
fun GenericInfoContent(
    modifier: Modifier = Modifier,
    icon: IconToken?,
    title: String,
    description: String,
    textPrimary: String,
    onClickPrimary: () -> Unit,
    textSecondary: String? = null,
    onClickSecondary: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 18.dp, top = 24.dp, start = 24.dp, end = 24.dp),
    ) {
        icon?.let {
            Thumbnail(
                modifier = Modifier
                    .padding(bottom = 24.dp)
                    .size(80.dp),
                type = ThumbnailType.Icon(icon),
                size = ThumbnailSize.XLarge,
            )
        }

        Text(
            text = title,
            style = DashlaneTheme.typography.titleSectionLarge,
            color = DashlaneTheme.colors.textNeutralCatchy,
        )
        Text(
            modifier = Modifier
                .padding(top = 8.dp),
            text = description,
            style = DashlaneTheme.typography.bodyStandardRegular,
            color = DashlaneTheme.colors.textNeutralStandard,
        )

        Spacer(modifier = Modifier.weight(1f))

        if (textSecondary != null && onClickSecondary != null) {
            ButtonMediumBar(
                modifier = Modifier.padding(top = 24.dp),
                primaryText = textPrimary,
                secondaryText = textSecondary,
                onPrimaryButtonClick = onClickPrimary,
                onSecondaryButtonClick = onClickSecondary,
            )
        } else {
            ButtonMedium(
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(top = 24.dp),
                onClick = onClickPrimary,
                intensity = Intensity.Catchy,
                layout = ButtonLayout.TextOnly(
                    text = textPrimary,
                ),
            )
        }
    }
}

@Preview
@Composable
private fun GenericInfoContentPreview() {
    DashlanePreview {
        GenericInfoContent(
            icon = IconTokens.checkmarkOutlined,
            title = "Title",
            description = "Description",
            textPrimary = "Primary",
            onClickPrimary = { },
            textSecondary = "Secondary",
            onClickSecondary = { },
        )
    }
}

@Preview
@Composable
private fun GenericInfoContentNoIconPreview() {
    DashlanePreview {
        GenericInfoContent(
            icon = null,
            title = "Title",
            description = "Description",
            textPrimary = "Primary",
            onClickPrimary = { },
            textSecondary = "Secondary",
            onClickSecondary = { },
        )
    }
}