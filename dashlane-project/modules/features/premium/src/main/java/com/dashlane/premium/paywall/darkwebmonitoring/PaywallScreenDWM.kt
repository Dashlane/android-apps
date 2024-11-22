package com.dashlane.premium.paywall.darkwebmonitoring

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dashlane.design.iconography.IconTokens
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.hermes.generated.definitions.AnyPage
import com.dashlane.premium.R
import com.dashlane.premium.offer.common.model.OfferType
import com.dashlane.premium.paywall.common.PaywallIntroScreen
import com.dashlane.premium.paywall.common.PaywallIntroState
import com.dashlane.premium.paywall.common.PaywallViewModel
import com.dashlane.ui.activities.intro.DescriptionItem
import com.dashlane.ui.activities.intro.LinkItem

@Composable
fun PaywallScreenDWM(
    modifier: Modifier = Modifier,
    intro: PaywallIntroState,
    navigateUp: () -> Unit,
    navigateToOffer: () -> Unit,
    onCancelClick: () -> Unit,
    onClickLink: (LinkItem) -> Unit
) {
    PaywallIntroScreen(
        modifier = modifier,
        intro = intro,
        navigateUp = navigateUp,
        onNegativeButtonClicked = onCancelClick,
        onPositiveButtonClicked = navigateToOffer,
        onClickLink = onClickLink
    ) {
        Image(
            painter = painterResource(id = R.drawable.illu_premium_dwm),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .size(180.dp)
                .clip(RectangleShape)
        )
    }
}

@Preview
@Composable
private fun PaywallScreenDWMPreview() {
    DashlanePreview {
        PaywallScreenDWM(
            intro = PaywallIntroState(
                title = R.string.paywall_intro_dwm_title,
                descriptionList = listOf(
                    DescriptionItem(
                        imageIconToken = IconTokens.featureDarkWebMonitoringOutlined,
                        titleResId = R.string.paywall_intro_dwm_message_1
                    ),
                    DescriptionItem(
                        imageIconToken = IconTokens.notificationOutlined,
                        titleResId = R.string.paywall_intro_dwm_message_2
                    ),
                    DescriptionItem(
                        imageIconToken = IconTokens.lockOutlined,
                        titleResId = R.string.paywall_intro_dwm_message_3
                    )
                ),
                linkList = listOf(PaywallViewModel.SeeAllPlanLinkItem),
                page = AnyPage.PAYWALL_DARK_WEB_MONITORING,
                goToOfferCTA = R.string.paywall_intro_buy_advance_cta,
                closeCTA = R.string.paywall_intro_close_cta,
                destinationOfferType = OfferType.PREMIUM,
                ctaListForLogs = emptyList()
            ),
            navigateUp = { },
            navigateToOffer = { },
            onCancelClick = {},
            onClickLink = {}
        )
    }
}
