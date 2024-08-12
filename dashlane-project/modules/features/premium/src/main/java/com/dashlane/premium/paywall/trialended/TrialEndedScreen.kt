package com.dashlane.premium.paywall.trialended

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.dashlane.design.component.Illustration
import com.dashlane.design.iconography.IconTokens
import com.dashlane.design.illustrations.IllustrationTokens
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.tooling.BooleanProvider
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.help.HelpCenterLink
import com.dashlane.premium.R
import com.dashlane.ui.activities.intro.DescriptionItem
import com.dashlane.ui.activities.intro.IntroScreen
import com.dashlane.ui.activities.intro.LinkItem
import com.dashlane.ui.common.compose.components.LoadingScreen

@Composable
fun TrialEndedScreen(
    trialEndedState: FreeTrialScreenState,
    onClickLink: (LinkItem) -> Unit = {},
    onCloseClick: () -> Unit,
    onSeePlansClick: () -> Unit,
) {
    when (trialEndedState) {
        FreeTrialScreenState.Init -> LoadingScreen(title = "")
        is FreeTrialScreenState.Loaded -> IntroScreen(
            illustration = {
                Illustration(
                    token = IllustrationTokens.protectMoreThanPasswords,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            shape = RoundedCornerShape(8.dp),
                            color = DashlaneTheme.colors.containerAgnosticNeutralQuiet
                        )
                )
            },
            titleResId = trialEndedState.titleResId,
            titleHeader = trialEndedState.titleHeaderResId,
            descriptionItems = trialEndedState.descriptionItems,
            linkItems = trialEndedState.linkItems,
            positiveButtonResId = trialEndedState.primaryButtonResId,
            onClickPositiveButton = onSeePlansClick,
            negativeButtonResId = trialEndedState.secondaryButtonResId,
            onClickNegativeButton = onCloseClick,
            onNavigationClick = onCloseClick,
            onClickLink = onClickLink
        )
    }
}

@Composable
@Preview
fun TrialEndedScreenPreview(@PreviewParameter(BooleanProvider::class) isUserFrozen: Boolean) {
    DashlanePreview {
        TrialEndedScreen(
            trialEndedState = FreeTrialScreenState.Loaded(
                titleResId = if (isUserFrozen) {
                    R.string.trial_ended_user_title_for_frozen_account
                } else {
                    R.string.trial_ended_user_title
                },
                titleHeaderResId = if (isUserFrozen) {
                    R.string.trial_ended_user_title_header_for_frozen_account
                } else {
                    R.string.trial_ended_user_title_header
                },
                descriptionItems = listOf(
                    DescriptionItem(
                        imageIconToken = IconTokens.itemLoginOutlined,
                        titleResId = R.string.frozen_account_paywall_benefit_unlimited_logins_and_passkeys
                    ),
                    DescriptionItem(
                        imageIconToken = IconTokens.featureAuthenticatorOutlined,
                        titleResId = R.string.frozen_account_paywall_benefit_unlimited_devices
                    ),
                    DescriptionItem(
                        imageIconToken = IconTokens.featureDarkWebMonitoringOutlined,
                        titleResId = R.string.frozen_account_paywall_benefit_dark_web_monitoring_and_vpn
                    )
                ),
                linkItems = listOf(
                    LinkItem.ExternalLinkItem(
                        linkResId = R.string.frozen_account_paywall_read_only_redirection,
                        link = HelpCenterLink.ARTICLE_ABOUT_FREE_PLAN_CHANGES.uri.toString()
                    )
                ),
                primaryButtonResId = R.string.frozen_account_paywall_cta_see_plans,
                secondaryButtonResId = R.string.frozen_account_paywall_cta_close
            ),
            onClickLink = {},
            onSeePlansClick = {},
            onCloseClick = {}
        )
    }
}
