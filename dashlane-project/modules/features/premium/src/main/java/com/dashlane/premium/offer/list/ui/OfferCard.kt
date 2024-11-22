package com.dashlane.premium.offer.list.ui

import PricingBox
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dashlane.design.component.Badge
import com.dashlane.design.component.HtmlText
import com.dashlane.design.component.Icon
import com.dashlane.design.component.Text
import com.dashlane.design.component.cardBackground
import com.dashlane.design.iconography.IconToken
import com.dashlane.design.iconography.IconTokens
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.color.Intensity
import com.dashlane.design.theme.color.Mood
import com.dashlane.design.theme.tooling.DashlanePreview

@Composable
fun OfferCard(
    iconToken: IconToken? = null,
    badgeText: String? = null,
    title: String,
    currentPlanLabel: String? = null,
    billedPrice: String,
    barredPrice: String? = null,
    additionalInfo: String? = null,
    description: String,
    descriptionMood: Mood? = null,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .cardBackground()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        badgeText?.let {
            Badge(
                text = it,
                mood = Mood.Positive,
                intensity = Intensity.Quiet
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    iconToken?.let { Icon(token = it, contentDescription = null) }
                    Text(
                        text = title,
                        style = DashlaneTheme.typography.titleSectionLarge
                    )
                }

                currentPlanLabel?.let {
                    Text(
                        text = it,
                        style = DashlaneTheme.typography.bodyHelperRegular
                    )
                }
            }
            PricingBox(
                billedPrice = billedPrice,
                barredPrice = barredPrice,
                additionalInfo = additionalInfo
            )
        }
        HtmlText(
            modifier = Modifier.padding(top = 24.dp),
            htmlText = description,
            style = DashlaneTheme.typography.bodyReducedRegular,
            color = descriptionMood?.textStandard ?: DashlaneTheme.colors.textNeutralQuiet
        )
    }
}

@Preview
@Composable
private fun OfferCardPreview() = DashlanePreview {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = DashlaneTheme.colors.backgroundAlternate)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        
        OfferCard(
            title = "Family",
            billedPrice = "$39 /year",
            description = "Protect the whole family with <strong>6 individual Premium accounts</strong> for one low price.",
            onClick = {}
        )
        
        OfferCard(
            iconToken = IconTokens.lockOutlined,
            title = "Free",
            billedPrice = "$0",
            description = "You’re over this plan’s <strong>25-password</strong> limit. Select a different plan for unlimited storage.",
            descriptionMood = Mood.Warning,
            onClick = {}
        )

        
        OfferCard(
            badgeText = "20% off",
            title = "Premium",
            currentPlanLabel = "Current trial",
            billedPrice = "$59 /year",
            barredPrice = "$79 /year",
            additionalInfo = "Then $79 /year",
            description = "Get unlimited logins synced across <strong>unlimited devices</strong>, plus Dark Web Monitoring and VPN " +
                "protection.",
            onClick = {}
        )
    }
}