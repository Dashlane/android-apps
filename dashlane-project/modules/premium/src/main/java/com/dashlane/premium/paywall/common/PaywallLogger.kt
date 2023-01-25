package com.dashlane.premium.paywall.common

interface PaywallLogger {
    var trackingKey: String
    fun onShowPaywall(presenter: PaywallPresenter)
    fun onLeaving()
    fun onClickSeeAllOptions()
    fun onClickUpgrade()
    fun onClickClose()
}