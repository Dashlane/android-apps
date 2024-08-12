package com.dashlane.premium.paywall.frozenaccount

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dashlane.design.component.Illustration
import com.dashlane.design.iconography.IconTokens
import com.dashlane.design.illustrations.IllustrationTokens
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.hermes.generated.definitions.AnyPage
import com.dashlane.premium.R
import com.dashlane.premium.paywall.common.PaywallIntroScreen
import com.dashlane.premium.paywall.common.PaywallIntroState
import com.dashlane.ui.activities.intro.DescriptionItem
import com.dashlane.ui.activities.intro.LinkItem

@Composable
fun PaywallScreenFrozenAccount(
    intro: PaywallIntroState,
    navigateUp: () -> Unit,
    onClickLink: (LinkItem) -> Unit,
    onCloseClick: () -> Unit,
    onAllOfferClick: () -> Unit,
) {
    PaywallIntroScreen(
        intro = intro,
        navigateUp = navigateUp,
        onNegativeButtonClicked = onCloseClick,
        onPositiveButtonClicked = onAllOfferClick,
        onClickLink = onClickLink
    ) {
        Illustration(
            token = IllustrationTokens.protectMoreThanPasswords,
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    shape = RoundedCornerShape(8.dp),
                    color = DashlaneTheme.colors.containerAgnosticNeutralQuiet
                )
        )
    }
}

@Composable
@Preview
fun TrialEndedAnnouncementScreenPreview() {
    DashlanePreview {
        PaywallScreenFrozenAccount(
            intro = PaywallIntroState(
                title = R.string.frozen_account_paywall_title,
                titleHeader = R.string.frozen_account_paywall_title_header,
                page = AnyPage.PAYWALL_FROZEN_ACCOUNT,
                goToOfferCTA = R.string.frozen_account_paywall_cta_see_plans,
                closeCTA = R.string.frozen_account_paywall_cta_close,
                destinationOfferType = null,
                linkList = listOf(
                    LinkItem.ExternalLinkItem(
                        linkResId = R.string.frozen_account_paywall_read_only_redirection,
                        link = ""
                    )
                ),
                descriptionList = listOf(
                    DescriptionItem(
                        imageIconToken = IconTokens.itemLoginOutlined,
                        titleResId = com.dashlane.frozenaccount.R.string.frozen_account_paywall_benefit_unlimited_logins_and_passkeys
                    ),
                    DescriptionItem(
                        imageIconToken = IconTokens.featureAuthenticatorOutlined,
                        titleResId = com.dashlane.frozenaccount.R.string.frozen_account_paywall_benefit_unlimited_devices
                    ),
                    DescriptionItem(
                        imageIconToken = IconTokens.featureDarkWebMonitoringOutlined,
                        titleResId = com.dashlane.frozenaccount.R.string.frozen_account_paywall_benefit_dark_web_monitoring_and_vpn
                    )
                ),
                ctaListForLogs = emptyList()
            ),
            navigateUp = {},
            onClickLink = {},
            onCloseClick = {},
            onAllOfferClick = {}
        )
    }
}
