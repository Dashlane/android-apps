package com.dashlane.premium.paywall.common

interface PaywallLogger {
    fun onLeaving()
    fun onClickSeeAllOptions()
    fun onClickUpgrade()
    fun onClickClose()
}