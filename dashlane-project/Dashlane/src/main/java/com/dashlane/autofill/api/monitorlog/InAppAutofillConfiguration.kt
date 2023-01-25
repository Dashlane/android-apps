package com.dashlane.autofill.api.monitorlog

import com.dashlane.inapplogin.InAppLoginManager
import javax.inject.Inject



class InAppAutofillConfiguration @Inject constructor(
    private val inAppLoginManager: InAppLoginManager
) : AutofillConfiguration {

    override fun hasAccessibilityAutofillEnabled(): Boolean =
        inAppLoginManager.isEnable(InAppLoginManager.TYPE_ACCESSIBILITY) ||
                inAppLoginManager.isEnable(InAppLoginManager.TYPE_NO_OP_ACCESSIBILITY)

    override fun hasAutofillByApiEnabled(): Boolean = inAppLoginManager.isEnable(InAppLoginManager.TYPE_AUTO_FILL_API)
}