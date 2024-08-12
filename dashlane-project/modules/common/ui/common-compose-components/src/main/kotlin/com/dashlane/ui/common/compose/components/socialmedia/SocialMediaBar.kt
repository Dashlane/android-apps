package com.dashlane.ui.common.compose.components.socialmedia

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dashlane.design.component.ButtonLayout
import com.dashlane.design.component.ButtonMedium
import com.dashlane.design.theme.color.Intensity
import com.dashlane.design.theme.color.Mood
import com.dashlane.design.theme.tooling.DashlanePreview
import java.net.URL

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SocialMediaBar(
    modifier: Modifier = Modifier,
    onClick: (URL) -> Unit,
    items: List<DashlaneSocialMedia>,
) {
    FlowRow(
        Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        items.forEach { socialMedia ->
            ButtonMedium(
                modifier = modifier,
                onClick = { onClick(socialMedia.url) },
                intensity = Intensity.Supershy,
                mood = Mood.Neutral,
                layout = ButtonLayout.IconOnly(
                    iconToken = socialMedia.iconToken,
                    contentDescription = stringResource(socialMedia.titleId)
                ),
            )
        }
    }
}

@Composable
@Preview
fun SettingSocialMediaLinkPreview() {
    DashlanePreview {
        SocialMediaBar(
            onClick = {},
            items = DashlaneSocialMedia.entries
        )
    }
}

@Composable
@Preview
private fun SettingSocialMediaLinkLimitedWidthPreview() {
    DashlanePreview {
        Box(Modifier.width(230.dp)) {
            SocialMediaBar(
                onClick = {},
                items = DashlaneSocialMedia.entries
            )
        }
    }
}