package com.dashlane.premium.paywall.common

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.dashlane.accountstatus.AccountStatus
import com.dashlane.design.iconography.IconTokens
import com.dashlane.frozenaccount.R.string
import com.dashlane.help.HelpCenterLink
import com.dashlane.hermes.generated.definitions.AnyPage
import com.dashlane.hermes.generated.definitions.CallToAction.ALL_OFFERS
import com.dashlane.hermes.generated.definitions.CallToAction.CANCEL
import com.dashlane.hermes.generated.definitions.CallToAction.CLOSE
import com.dashlane.hermes.generated.definitions.CallToAction.PREMIUM_OFFER
import com.dashlane.navigation.Navigator
import com.dashlane.navigation.paywall.PaywallIntroType
import com.dashlane.premium.R
import com.dashlane.premium.offer.common.UserBenefitStatusProvider
import com.dashlane.premium.offer.common.model.OfferType
import com.dashlane.premium.offer.common.model.UserBenefitStatus
import com.dashlane.ui.activities.intro.DescriptionItem
import com.dashlane.ui.activities.intro.LinkItem
import com.dashlane.util.inject.OptionalProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PaywallViewModel @Inject constructor(
    private val accountStatusProvider: OptionalProvider<AccountStatus>,
    private val savedStateHandle: SavedStateHandle,
    private val logger: PaywallLogger,
    private val userBenefitStatusProvider: UserBenefitStatusProvider,
    private val navigator: Navigator
) : ViewModel() {
    private val isUserInTrial: Boolean
        get() = userBenefitStatusProvider.getFormattedStatus(accountStatusProvider.get()).type == UserBenefitStatus.Type.Trial

    val paywallIntroState: PaywallIntroState
        get() = when (paywallIntroType) {
            PaywallIntroType.DARK_WEB_MONITORING -> getDarkWebMonitoringIntro()
            PaywallIntroType.VPN -> getVPNIntro()
            PaywallIntroType.FROZEN_ACCOUNT -> getFrozenAccountIntro()
        }

    val paywallIntroType: PaywallIntroType
        get() = savedStateHandle[PaywallActivity.PAYWALL_INTRO_TYPE_ARG]!!

    object SeeAllPlanLinkItem :
        LinkItem.InternalLinkItem(R.string.paywall_intro_see_plan_options_cta)

    private fun getDarkWebMonitoringIntro(): PaywallIntroState =
        PaywallIntroState(
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
            linkList = listOf(SeeAllPlanLinkItem),
            page = AnyPage.PAYWALL_DARK_WEB_MONITORING,
            goToOfferCTA = R.string.paywall_intro_buy_premium_cta,
            closeCTA = R.string.paywall_intro_close_cta,
            destinationOfferType = OfferType.PREMIUM,
            ctaListForLogs = listOf(ALL_OFFERS, CANCEL, PREMIUM_OFFER)
        )

    private fun getVPNIntro(): PaywallIntroState =
        PaywallIntroState(
            title = when {
                isUserInTrial -> R.string.paywall_intro_vpn_third_party_title_in_trial
                else -> R.string.paywall_intro_vpn_third_party_title
            },
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
                    HOTSPOT_SHIELD_URL
                )
            ),
            page = AnyPage.PAYWALL_VPN,
            goToOfferCTA = R.string.paywall_intro_buy_premium_cta,
            closeCTA = R.string.paywall_intro_close_cta,
            destinationOfferType = OfferType.PREMIUM,
            ctaListForLogs = listOf(CANCEL, PREMIUM_OFFER)
        )

    private fun getFrozenAccountIntro(): PaywallIntroState = PaywallIntroState(
        title = R.string.frozen_account_paywall_title,
        titleHeader = R.string.frozen_account_paywall_title_header,
        descriptionList = listOf(
            DescriptionItem(
                imageIconToken = IconTokens.itemLoginOutlined,
                titleResId = string.frozen_account_paywall_benefit_unlimited_logins_and_passkeys
            ),
            DescriptionItem(
                imageIconToken = IconTokens.featureAuthenticatorOutlined,
                titleResId = string.frozen_account_paywall_benefit_unlimited_devices
            ),
            DescriptionItem(
                imageIconToken = IconTokens.featureDarkWebMonitoringOutlined,
                titleResId = string.frozen_account_paywall_benefit_dark_web_monitoring_and_vpn
            )
        ),
        linkList = listOf(
            LinkItem.ExternalLinkItem(
                linkResId = R.string.frozen_account_paywall_read_only_redirection,
                link = HelpCenterLink.ARTICLE_ABOUT_FREE_PLAN_CHANGES.uri.toString()
            )
        ),
        page = AnyPage.PAYWALL_FROZEN_ACCOUNT,
        goToOfferCTA = R.string.frozen_account_paywall_cta_see_plans,
        closeCTA = R.string.frozen_account_paywall_cta_close,
        destinationOfferType = null,
        ctaListForLogs = listOf(CLOSE, ALL_OFFERS)
    )

    fun onLeaving() = logger.onLeaving(paywallIntroState.ctaListForLogs)

    fun onClickUpgrade() {
        logger.onClickUpgrade(paywallIntroState.ctaListForLogs)
        navigator.goToOffers(paywallIntroState.destinationOfferType?.toString())
    }

    fun onNavigateUp() = logger.onNavigateUp(paywallIntroState.ctaListForLogs)

    fun onClickClose() = logger.onClickClose(paywallIntroState.ctaListForLogs)

    fun onClickCancel() = logger.onClickCancel(paywallIntroState.ctaListForLogs)

    fun handleOnClickLink(linkItem: LinkItem) {
        when (linkItem) {
            is LinkItem.ExternalLinkItem -> {
                navigator.goToWebView(linkItem.link)
            }
            is SeeAllPlanLinkItem -> onClickAllOffers()
            else -> Unit
        }
    }

    fun onClickAllOffers() {
        logger.onClickSeeAllOptions(paywallIntroState.ctaListForLogs)
        navigator.goToOffers()
    }

    companion object {
        internal const val HOTSPOT_SHIELD_URL = "https://www.hotspotshield.com/"
    }
}