package com.dashlane.ui.common.compose.components.banner

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dashlane.design.component.Icon
import com.dashlane.design.component.Text
import com.dashlane.design.iconography.IconToken
import com.dashlane.design.iconography.IconTokens
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.color.Mood
import com.dashlane.design.theme.tooling.DashlanePreview

@Composable
fun VaultBanner(iconToken: IconToken, title: String?, description: String, mood: Mood, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .background(color = mood.containerExpressiveQuietIdle)
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clickable(
                indication = rememberRipple(color = mood.textQuiet.value),
                interactionSource = remember { MutableInteractionSource() }
            ) { onClick() }
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Top,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                modifier = Modifier
                    .padding(vertical = with(LocalDensity.current) { 1.sp.toDp() })
                    .size(with(LocalDensity.current) { 16.sp.toDp() }),
                token = iconToken,
                contentDescription = null,
                tint = mood.textStandard
            )
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                title?.let {
                    Text(text = it, style = DashlaneTheme.typography.titleBlockSmall, color = mood.textStandard)
                }
                Text(text = description, style = DashlaneTheme.typography.bodyReducedRegular, color = mood.textStandard)
            }
        }
    }
}

private data class BannerPreviewData(val iconToken: IconToken, val title: String?, val description: String, val mood: Mood)

private class BannerDataProvider : PreviewParameterProvider<BannerPreviewData> {
    override val values = sequenceOf(
        BannerPreviewData(IconTokens.featureAutofillOutlined, null, "Autofill is not enabled", Mood.Neutral),
        BannerPreviewData(IconTokens.premiumOutlined, null, "5 passwords left in your Free plan.\nUpgrade to Premium", Mood.Brand),
        BannerPreviewData(IconTokens.premiumOutlined, null, "You’ve reached your Free plan password limit.\nUpgrade to premium", Mood.Warning),
        BannerPreviewData(
            IconTokens.feedbackFailOutlined,
            "Your account is read-only",
            "You have over 25 passwords saved. Remove passwords or upgrade to regain" + " full access to your account.",
            Mood.Danger
        ),
    )
}

@Preview
@Composable
private fun VaultBannerPreview(@PreviewParameter(BannerDataProvider::class) bannerContent: BannerPreviewData) {
    DashlanePreview {
        VaultBanner(
            iconToken = bannerContent.iconToken,
            title = bannerContent.title,
            description = bannerContent.description,
            mood = bannerContent.mood
        ) {}
    }
}
