package com.dashlane.premium.offer.common

interface InAppBillingDebugPreference {

    fun isAllPurchaseActionsAllowed(): Boolean

    fun getOverridingProratedMode(): Int?
}