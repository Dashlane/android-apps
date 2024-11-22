package com.dashlane.premium.paywall.common

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.dashlane.navigation.paywall.PaywallIntroType
import com.dashlane.premium.paywall.darkwebmonitoring.PaywallScreenDWM
import com.dashlane.premium.paywall.frozenaccount.PaywallScreenFrozenAccount
import com.dashlane.premium.paywall.vpn.PaywallScreenVPN

@Composable
fun PaywallScreen(
    viewModel: PaywallViewModel,
    contentPadding: PaddingValues = PaddingValues(),
    navigateUp: () -> Unit,
    navigateToOffer: () -> Unit,
    onCloseClick: () -> Unit,
    onCancelClick: () -> Unit,
    onAllOffersClick: () -> Unit,
) {
    when (viewModel.paywallIntroType) {
        PaywallIntroType.DARK_WEB_MONITORING -> PaywallScreenDWM(
            modifier = Modifier.padding(contentPadding),
            intro = viewModel.paywallIntroState,
            navigateUp = navigateUp,
            navigateToOffer = navigateToOffer,
            onCancelClick = onCancelClick,
            onClickLink = { linkItem -> viewModel.handleOnClickLink(linkItem) }
        )
        PaywallIntroType.VPN -> PaywallScreenVPN(
            modifier = Modifier.padding(contentPadding),
            intro = viewModel.paywallIntroState,
            navigateUp = navigateUp,
            navigateToOffer = navigateToOffer,
            onCancelClick = onCancelClick,
            onClickLink = { linkItem -> viewModel.handleOnClickLink(linkItem) }
        )
        PaywallIntroType.FROZEN_ACCOUNT -> PaywallScreenFrozenAccount(
            modifier = Modifier.padding(contentPadding),
            intro = viewModel.paywallIntroState,
            navigateUp = navigateUp,
            onAllOfferClick = onAllOffersClick,
            onCloseClick = onCloseClick,
            onClickLink = { linkItem -> viewModel.handleOnClickLink(linkItem) }
        )
    }
}