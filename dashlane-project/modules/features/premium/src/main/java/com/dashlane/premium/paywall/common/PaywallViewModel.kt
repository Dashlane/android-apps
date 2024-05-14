package com.dashlane.premium.paywall.common

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.dashlane.accountstatus.AccountStatus
import com.dashlane.design.iconography.IconTokens
import com.dashlane.hermes.generated.definitions.AnyPage
import com.dashlane.navigation.Navigator
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
        }

    val paywallIntroType: PaywallIntroType
        get() = savedStateHandle.get<PaywallIntroType>(PaywallActivity.PAYWALL_INTRO_TYPE_KEY)!!

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
            goToOfferCTA = R.string.paywall_intro_buy_advance_cta,
            destinationOfferType = OfferType.ADVANCED
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
            destinationOfferType = OfferType.PREMIUM
        )

    fun onLeaving() = logger.onLeaving()

    fun onClickUpgrade() {
        logger.onClickUpgrade()
        navigator.goToOffers(paywallIntroState.destinationOfferType.toString())
    }

    fun onClickClose() = logger.onClickClose()

    fun handleOnClickLink(linkItem: LinkItem) {
        when (linkItem) {
            is LinkItem.ExternalLinkItem -> {
                navigator.goToWebView(linkItem.link)
            }
            is SeeAllPlanLinkItem -> {
                logger.onClickSeeAllOptions()
                navigator.goToOffers()
            }
            else -> Unit
        }
    }

    companion object {
        internal const val HOTSPOT_SHIELD_URL = "https://www.hotspotshield.com/"
    }
}