package com.dashlane.autofill.api.monitorlog



interface AutofillConfiguration {
    fun hasAccessibilityAutofillEnabled(): Boolean

    fun hasAutofillByApiEnabled(): Boolean
}