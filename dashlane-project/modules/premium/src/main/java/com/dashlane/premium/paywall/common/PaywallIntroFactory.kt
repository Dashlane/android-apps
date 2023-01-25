package com.dashlane.premium.paywall.common

interface PaywallIntroFactory {
    fun get(type: PaywallIntroType, origin: String?): PaywallIntro
}