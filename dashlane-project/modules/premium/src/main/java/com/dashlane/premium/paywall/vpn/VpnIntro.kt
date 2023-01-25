package com.dashlane.premium.paywall.vpn

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import com.dashlane.hermes.generated.definitions.AnyPage
import com.dashlane.premium.R
import com.dashlane.premium.offer.common.FormattedPremiumStatusManager
import com.dashlane.premium.offer.common.model.UserBenefitStatus
import com.dashlane.premium.paywall.common.PaywallIntro

class VpnIntro(private val premiumStatus: FormattedPremiumStatusManager) : PaywallIntro {
    override val image = R.drawable.ic_paywall_vpn
    override val title = when {
        isUserInTrial() -> R.string.paywall_intro_vpn_third_party_title_in_trial
        else -> R.string.paywall_intro_vpn_third_party_title
    }
    override val message = when {
        isUserInTrial() -> R.string.paywall_intro_vpn_third_party_message_in_trial
        else -> R.string.paywall_intro_vpn_third_party_message
    }
    override val messageFormatArgs: Array<Any> = emptyArray()
    override val trackingKey = "vpn"
    override val page = AnyPage.PAYWALL_VPN

    @SuppressLint("InflateParams")
    override fun provideDetailsView(context: Context) = LayoutInflater.from(context)
        .inflate(R.layout.include_vpn_third_party_intro_details, null)

    override fun onShowPaywall() {
        
    }

    override fun onClickSeeAllOptions() = Unit

    override fun onClickClose() = Unit

    override fun onClickUpgrade() = Unit

    private fun isUserInTrial(): Boolean =
        premiumStatus.getFormattedStatus().type == UserBenefitStatus.Type.Trial
}