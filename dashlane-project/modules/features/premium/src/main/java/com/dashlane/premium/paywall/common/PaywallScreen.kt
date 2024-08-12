package com.dashlane.premium.paywall.common

import androidx.compose.runtime.Composable
import com.dashlane.navigation.paywall.PaywallIntroType
import com.dashlane.premium.paywall.darkwebmonitoring.PaywallScreenDWM
import com.dashlane.premium.paywall.frozenaccount.PaywallScreenFrozenAccount
import com.dashlane.premium.paywall.vpn.PaywallScreenVPN

@Composable
fun PaywallScreen(
    viewModel: PaywallViewModel,
    navigateUp: () -> Unit,
    navigateToOffer: () -> Unit,
    onCloseClick: () -> Unit,
    onCancelClick: () -> Unit,
    onAllOffersClick: () -> Unit,
) {
    when (viewModel.paywallIntroType) {
        PaywallIntroType.DARK_WEB_MONITORING -> PaywallScreenDWM(
            intro = viewModel.paywallIntroState,
            navigateUp = navigateUp,
            navigateToOffer = navigateToOffer,
            onCancelClick = onCancelClick,
            onClickLink = { linkItem -> viewModel.handleOnClickLink(linkItem) }
        )
        PaywallIntroType.VPN -> PaywallScreenVPN(
            intro = viewModel.paywallIntroState,
            navigateUp = navigateUp,
            navigateToOffer = navigateToOffer,
            onCancelClick = onCancelClick,
            onClickLink = { linkItem -> viewModel.handleOnClickLink(linkItem) }
        )
        PaywallIntroType.FROZEN_ACCOUNT -> PaywallScreenFrozenAccount(
            intro = viewModel.paywallIntroState,
            navigateUp = navigateUp,
            onAllOfferClick = onAllOffersClick,
            onCloseClick = onCloseClick,
            onClickLink = { linkItem -> viewModel.handleOnClickLink(linkItem) }
        )
    }
}