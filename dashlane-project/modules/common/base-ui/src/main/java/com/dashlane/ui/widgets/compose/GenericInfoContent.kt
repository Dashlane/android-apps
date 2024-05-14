package com.dashlane.ui.widgets.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dashlane.design.component.ButtonLayout
import com.dashlane.design.component.ButtonMedium
import com.dashlane.design.component.ButtonMediumBar
import com.dashlane.design.component.Text
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.color.Intensity
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.ui.R

@Composable
fun GenericInfoContent(
    modifier: Modifier = Modifier,
    icon: Painter?,
    title: String,
    description: String,
    textPrimary: String,
    onClickPrimary: () -> Unit,
    textSecondary: String? = null,
    onClickSecondary: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.weight(1f))

        icon?.let {
            Image(
                modifier = Modifier
                    .height(96.dp)
                    .padding(top = 16.dp),
                painter = icon,
                contentDescription = null,
                contentScale = ContentScale.FillHeight,
                colorFilter = ColorFilter.tint(DashlaneTheme.colors.textBrandQuiet.value)
            )
        }

        Text(
            modifier = Modifier
                .padding(top = 16.dp, bottom = 16.dp),
            text = title,
            style = DashlaneTheme.typography.titleSectionLarge,
            color = DashlaneTheme.colors.textNeutralCatchy,

            )
        Text(
            modifier = Modifier
                .padding(bottom = 16.dp),
            text = description,
            style = DashlaneTheme.typography.bodyStandardRegular,
            color = DashlaneTheme.colors.textNeutralStandard,

            )

        Spacer(modifier = Modifier.weight(1.75f))

        if (textSecondary != null && onClickSecondary != null) {
            ButtonMediumBar(
                primaryText = textPrimary,
                secondaryText = textSecondary,
                onPrimaryButtonClick = onClickPrimary,
                onSecondaryButtonClick = onClickSecondary
            )
        } else {
            ButtonMedium(
                modifier = Modifier.align(Alignment.End),
                onClick = onClickPrimary,
                intensity = Intensity.Catchy,
                layout = ButtonLayout.TextOnly(
                    text = textPrimary
                )
            )
        }
    }
}

@Preview
@Composable
fun GenericInfoContentPreview() {
    DashlanePreview {
        GenericInfoContent(
            icon = painterResource(id = R.drawable.ic_check),
            title = "Title",
            description = "Description",
            textPrimary = "Primary",
            onClickPrimary = { },
            textSecondary = "Secondary",
            onClickSecondary = { }
        )
    }
}

@Preview
@Composable
fun GenericInfoContentNoIconPreview() {
    DashlanePreview {
        GenericInfoContent(
            icon = null,
            title = "Title",
            description = "Description",
            textPrimary = "Primary",
            onClickPrimary = { },
            textSecondary = "Secondary",
            onClickSecondary = { }
        )
    }
}
