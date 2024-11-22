package com.dashlane.ui.common.compose.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dashlane.design.component.ButtonMediumBar
import com.dashlane.design.component.InfoboxMedium
import com.dashlane.design.component.Text
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.color.Mood
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.ui.commoncomposecomponents.R

@Composable
fun WarningContent(
    modifier: Modifier = Modifier,
    painter: Painter,
    contentDescription: String? = null,
    title: String,
    description: String,
    infoBox: String?,
    primaryButtonText: String,
    secondaryButtonText: String,
    onPrimaryButtonClick: () -> Unit,
    onSecondaryButtonClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.weight(1f))

        Image(
            modifier = Modifier.fillMaxWidth(),
            painter = painter,
            contentDescription = contentDescription,
            colorFilter = ColorFilter.tint(DashlaneTheme.colors.oddityBrand),
            alignment = Alignment.Center
        )

        Text(
            text = title,
            style = DashlaneTheme.typography.titleSectionMedium,
            textAlign = TextAlign.Center,
            color = DashlaneTheme.colors.textNeutralCatchy,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 32.dp, bottom = 16.dp)
        )
        Text(
            text = description,
            style = DashlaneTheme.typography.bodyReducedRegular,
            textAlign = TextAlign.Center,
            color = DashlaneTheme.colors.textNeutralStandard,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )
        infoBox?.let {
            InfoboxMedium(
                title = infoBox,
                mood = Mood.Brand
            )
        }
        Spacer(modifier = Modifier.weight(1f))

        ButtonMediumBar(
            primaryText = primaryButtonText,
            onPrimaryButtonClick = onPrimaryButtonClick,
            secondaryText = secondaryButtonText,
            onSecondaryButtonClick = onSecondaryButtonClick
        )
    }
}

@Preview
@Composable
private fun WarningContentPreview() {
    DashlanePreview {
        WarningContent(
            painter = painterResource(id = R.drawable.sample_thumbnail_gitlab),
            title = "Title",
            description = "Description",
            infoBox = "Info Box",
            primaryButtonText = "Primary",
            secondaryButtonText = "Secondary",
            onPrimaryButtonClick = {},
            onSecondaryButtonClick = {},
        )
    }
}