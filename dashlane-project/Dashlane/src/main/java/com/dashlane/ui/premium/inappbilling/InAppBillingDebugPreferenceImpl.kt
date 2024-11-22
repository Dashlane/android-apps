package com.dashlane.ui.premium.inappbilling

import com.dashlane.debug.services.DaDaDaBilling
import com.dashlane.premium.offer.common.InAppBillingDebugPreference
import javax.inject.Inject

class InAppBillingDebugPreferenceImpl @Inject constructor(private val dadadaBilling: DaDaDaBilling) : InAppBillingDebugPreference {
    override fun isAllPurchaseActionsAllowed() = dadadaBilling.isAllPurchaseActionsAllowed

    override fun getOverridingProratedMode() = dadadaBilling.overridingProratedMode
}