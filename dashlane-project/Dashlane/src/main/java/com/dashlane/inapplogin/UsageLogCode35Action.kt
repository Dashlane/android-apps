package com.dashlane.inapplogin

import com.dashlane.useractivity.log.usage.UsageLogRepository
import com.dashlane.useractivity.log.usage.UsageLogCode35

object UsageLogCode35Action {
    const val COPY_LOGIN = "copyLogin"
    const val COPY_PASSWORD = "copyPassword"
    const val COPY_NUMBER = "copyNumber"
    const val COPY_SECURITY_CODE = "copyCVV"
    const val COPY_IBAN = "copyIban"
    const val COPY_BIC = "copyBic"

    const val SHOW_PASSWORD = "showPassword"
    const val SHOW_NUMBER = "showNumber"
    const val SHOW_SECURITY_CODE = "showCVV"
    const val SHOW_IBAN = "showIban"
    const val SHOW_BIC = "showBic"
    const val SHOW_NOTES = "showNotes"
    const val EXPAND_ASSOCIATED_DOMAINS = "expandAssociatedDomains"
}



internal fun sendAutofillActivationLog(usageLogRepository: UsageLogRepository?, inAppLoginManager: InAppLoginManager) {
    val autofillAction = if (!inAppLoginManager.hasAutofillApiDisabled()) {
        "True"
    } else {
        "False"
    }
    usageLogRepository?.enqueue(
        UsageLogCode35(
            type = "autofillState",
            action = autofillAction
        )
    )
}
