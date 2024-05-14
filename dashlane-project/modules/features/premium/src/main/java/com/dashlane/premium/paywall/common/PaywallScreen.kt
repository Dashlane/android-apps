package com.dashlane.premium.paywall.common

import android.content.res.Configuration
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dashlane.design.component.Icon
import com.dashlane.design.iconography.IconTokens
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.hermes.generated.definitions.AnyPage
import com.dashlane.premium.R
import com.dashlane.premium.offer.common.model.OfferType
import com.dashlane.ui.activities.intro.DescriptionItem
import com.dashlane.ui.activities.intro.IntroScreen
import com.dashlane.ui.activities.intro.LinkItem

@Composable
fun PaywallScreen(
    viewModel: PaywallViewModel,
    navigateUp: () -> Unit,
    navigateToOffer: () -> Unit
) {
    when (viewModel.paywallIntroType) {
        PaywallIntroType.DARK_WEB_MONITORING -> PaywallScreenDWM(
            intro = viewModel.paywallIntroState,
            navigateUp = navigateUp,
            navigateToOffer = navigateToOffer,
            onClickLink = { linkItem -> viewModel.handleOnClickLink(linkItem) }
        )
        PaywallIntroType.VPN -> PaywallScreenVPN(
            intro = viewModel.paywallIntroState,
            navigateUp = navigateUp,
            navigateToOffer = navigateToOffer,
            onClickLink = { linkItem -> viewModel.handleOnClickLink(linkItem) }
        )
    }
}

@Composable
fun PaywallScreenDWM(
    intro: PaywallIntroState,
    navigateUp: () -> Unit,
    navigateToOffer: () -> Unit,
    onClickLink: (LinkItem) -> Unit
) {
    PaywallIntroScreen(
        intro = intro,
        positiveButtonResId = intro.goToOfferCTA,
        navigateUp = navigateUp,
        navigateToOffer = navigateToOffer,
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

@Composable
fun PaywallScreenVPN(
    intro: PaywallIntroState,
    navigateUp: () -> Unit,
    navigateToOffer: () -> Unit,
    onClickLink: (LinkItem) -> Unit
) {
    PaywallIntroScreen(
        intro = intro,
        positiveButtonResId = intro.goToOfferCTA,
        navigateUp = navigateUp,
        navigateToOffer = navigateToOffer,
        onClickLink = onClickLink
    ) {
        val portrait = LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT
        if (portrait) {
            IllustrationVPN()
        } else {
            IllustrationVPNLandScape()
        }
    }
}

@Composable
private fun PaywallIntroScreen(
    intro: PaywallIntroState,
    @StringRes positiveButtonResId: Int,
    navigateUp: () -> Unit,
    navigateToOffer: () -> Unit,
    onClickLink: (LinkItem) -> Unit,
    illustrationView: @Composable () -> Unit
) {
    IntroScreen(
        titleResId = intro.title,
        descriptionItems = intro.descriptionList,
        linkResIds = intro.linkList,
        positiveButtonResId = positiveButtonResId,
        negativeButtonResId = R.string.paywall_intro_close_cta,
        onNavigationClick = navigateUp,
        onClickNegativeButton = navigateUp,
        onClickPositiveButton = navigateToOffer,
        onClickLink = onClickLink,
        illustration = illustrationView
    )
}

@Composable
private fun IllustrationVPN() {
    Column(
        modifier = Modifier.wrapContentSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo_lock_up),
            contentDescription = null
        )
        Icon(
            modifier = Modifier
                .padding(8.dp)
                .size(16.dp),
            token = IconTokens.actionCloseOutlined,
            contentDescription = null,
            tint = DashlaneTheme.colors.textNeutralStandard
        )
        Image(
            painter = painterResource(id = R.drawable.logo_hotspot_shield_seek),
            contentDescription = null
        )
    }
}

@Composable
private fun IllustrationVPNLandScape() {
    Row(
        modifier = Modifier.wrapContentSize(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo_lock_up),
            contentDescription = null
        )
        Icon(
            modifier = Modifier
                .padding(16.dp)
                .size(16.dp),
            token = IconTokens.actionCloseOutlined,
            contentDescription = null,
            tint = DashlaneTheme.colors.textNeutralStandard
        )
        Image(
            painter = painterResource(id = R.drawable.logo_hotspot_shield_seek),
            contentDescription = null
        )
    }
}

@Composable
@Preview
private fun PreviewPaywallScreenVPN() {
    DashlanePreview {
        PaywallScreenVPN(
            intro = PaywallIntroState(
                title = R.string.paywall_intro_vpn_third_party_title_in_trial,
                descriptionList = listOf(
                    DescriptionItem(
                        imageIconToken = IconTokens.featureVpnOutlined,
                        titleResId = R.string.paywall_intro_vpn_third_party_message_1
                    ),
                    DescriptionItem(
                        imageIconToken = IconTokens.healthPositiveOutlined,
                        titleResId = R.string.paywall_intro_vpn_third_party_message_2
                    ),
                    DescriptionItem(
                        imageIconToken = IconTokens.webOutlined,
                        titleResId = R.string.paywall_intro_vpn_third_party_message_3
                    )
                ),
                linkList = listOf(
                    LinkItem.ExternalLinkItem(
                        R.string.paywall_intro_vpn_learn_more_cta,
                        PaywallViewModel.HOTSPOT_SHIELD_URL
                    )
                ),
                page = AnyPage.PAYWALL_VPN,
                goToOfferCTA = R.string.paywall_intro_buy_premium_cta,
                destinationOfferType = OfferType.PREMIUM
            ),
            navigateUp = { },
            navigateToOffer = { },
            onClickLink = { }
        )
    }
}