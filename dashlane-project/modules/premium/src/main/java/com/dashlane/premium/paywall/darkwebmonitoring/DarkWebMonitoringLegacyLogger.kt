package com.dashlane.premium.paywall.darkwebmonitoring

interface DarkWebMonitoringLegacyLogger {
    fun onShowPremiumPrompt()
    fun onClickPremiumPromptGoPremium()
    fun getClickPremiumPromptClose()
}