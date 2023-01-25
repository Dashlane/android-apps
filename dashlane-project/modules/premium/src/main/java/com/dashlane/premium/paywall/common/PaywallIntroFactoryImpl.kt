package com.dashlane.premium.paywall.common

import com.dashlane.premium.offer.common.FormattedPremiumStatusManager
import com.dashlane.premium.paywall.darkwebmonitoring.DarkWebMonitoringIntro
import com.dashlane.premium.paywall.darkwebmonitoring.DarkWebMonitoringLegacyLoggerImpl
import com.dashlane.premium.paywall.vpn.VpnIntro
import com.dashlane.session.BySessionRepository
import com.dashlane.session.SessionManager
import com.dashlane.useractivity.log.usage.UsageLogRepository
import javax.inject.Inject

class PaywallIntroFactoryImpl @Inject constructor(
    private val formattedPremiumStatus: FormattedPremiumStatusManager,
    private val sessionManager: SessionManager,
    private val bySessionUsageLogRepository: BySessionRepository<UsageLogRepository>
) : PaywallIntroFactory {

    override fun get(type: PaywallIntroType, origin: String?) =
        when (type) {
            PaywallIntroType.DARK_WEB_MONITORING -> DarkWebMonitoringIntro(
                logger = DarkWebMonitoringLegacyLoggerImpl(
                    sessionManager = sessionManager,
                    bySessionUsageLogRepository = bySessionUsageLogRepository
                )
            )
            PaywallIntroType.VPN -> VpnIntro(
                premiumStatus = formattedPremiumStatus
            )
        }
}