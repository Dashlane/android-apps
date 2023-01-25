package com.dashlane.ui.premium.inappbilling

import com.dashlane.premium.offer.common.InAppBillingDebugPreference
import com.dashlane.debug.DaDaDa
import javax.inject.Inject

class InAppBillingDebugPreferenceImpl @Inject constructor(private val daDaDa: DaDaDa) : InAppBillingDebugPreference {
    override fun isAllPurchaseActionsAllowed() = daDaDa.isAllPurchaseActionsAllowed

    override fun getOverridingProratedMode() = daDaDa.overridingProratedMode
}