package com.dashlane.wear.vault.details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.wear.ui.DashlaneWearPreview
import com.dashlane.wear.ui.component.Emphasis
import com.dashlane.wear.ui.component.WearText
import com.dashlane.wear.vault.list.WearMiniDomainIcon
import com.dashlane.wear.vault.list.WearMiniDomainIconForPreview

@Composable
fun WearItemDetails(
    urlDomain: String,
    title: String,
    fields: List<ItemField>,
    modifier: Modifier = Modifier,
    emphasis: Emphasis = Emphasis.Low,
) {
    Column(
        modifier = modifier
            .background(
                color = emphasis.backgroundColor(),
                shape = RoundedCornerShape(24.dp),
            )
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(space = 8.dp),
    ) {
        Row(
            modifier = Modifier
                .padding(2.dp),
            horizontalArrangement = Arrangement.spacedBy(
                space = 8.dp,
                alignment = Alignment.CenterHorizontally,
            ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (!LocalInspectionMode.current) {
                WearMiniDomainIcon(urlDomain = urlDomain)
            } else {
                WearMiniDomainIconForPreview()
            }
            WearText(text = title)
        }
        fields.forEach { field ->
            Column {
                WearText(
                    text = field.title,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = DashlaneTheme.typography.bodyReducedRegular.copy(
                        fontWeight = FontWeight.Bold,
                    ),
                )
                WearText(
                    text = field.content,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = DashlaneTheme.typography.bodyReducedRegular,
                    color = DashlaneTheme.colors.textInverseQuiet,
                )
            }
        }
    }
}

data class ItemField(val title: String, val content: String)

@Composable
@Preview
private fun WearItemDetailsPreview() {
    DashlaneWearPreview {
        WearItemDetails(
            urlDomain = "spotify.com",
            title = "Github",
            fields = listOf(
                ItemField(
                    title = "Item Name",
                    content = "Github",
                ),
                ItemField(
                    title = "Username",
                    content = "randomemail@provider.com",
                ),
                ItemField(
                    title = "Website",
                    content = "www.github.com",
                ),
            ),
        )
    }
}