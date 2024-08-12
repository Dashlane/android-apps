package com.dashlane.premium.paywall.common

import androidx.compose.runtime.Composable
import com.dashlane.ui.activities.intro.IntroScreen
import com.dashlane.ui.activities.intro.LinkItem

@Composable
fun PaywallIntroScreen(
    intro: PaywallIntroState,
    navigateUp: () -> Unit,
    onNegativeButtonClicked: () -> Unit,
    onPositiveButtonClicked: () -> Unit,
    onClickLink: (LinkItem) -> Unit,
    illustrationView: @Composable () -> Unit
) {
    IntroScreen(
        titleResId = intro.title,
        titleHeader = intro.titleHeader,
        descriptionItems = intro.descriptionList,
        linkItems = intro.linkList,
        positiveButtonResId = intro.goToOfferCTA,
        negativeButtonResId = intro.closeCTA,
        onNavigationClick = navigateUp,
        onClickNegativeButton = onNegativeButtonClicked,
        onClickPositiveButton = onPositiveButtonClicked,
        onClickLink = onClickLink,
        illustration = illustrationView
    )
}