package com.dashlane.premium.paywall.common

import com.dashlane.hermes.generated.definitions.CallToAction

interface PaywallLogger {
    fun onLeaving(callToActionList: List<CallToAction>)
    fun onClickSeeAllOptions(callToActionList: List<CallToAction>)
    fun onClickUpgrade(callToActionList: List<CallToAction>)
    fun onNavigateUp(callToActionList: List<CallToAction>)
    fun onClickClose(callToActionList: List<CallToAction>)
    fun onClickCancel(callToActionList: List<CallToAction>)
}