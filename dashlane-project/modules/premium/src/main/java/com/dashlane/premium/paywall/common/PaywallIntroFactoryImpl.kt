package com.dashlane.premium.paywall.common

import com.dashlane.premium.offer.common.FormattedPremiumStatusManager
import com.dashlane.premium.paywall.darkwebmonitoring.DarkWebMonitoringIntro
import com.dashlane.premium.paywall.vpn.VpnIntro
import javax.inject.Inject

class PaywallIntroFactoryImpl @Inject constructor(
    private val formattedPremiumStatus: FormattedPremiumStatusManager
) : PaywallIntroFactory {

    override fun get(type: PaywallIntroType, origin: String?) =
        when (type) {
            PaywallIntroType.DARK_WEB_MONITORING -> DarkWebMonitoringIntro()
            PaywallIntroType.VPN -> VpnIntro(
                premiumStatus = formattedPremiumStatus
            )
        }
}