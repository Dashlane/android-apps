package com.dashlane.autofill.api.monitorlog



data class AutofillStakeholdersInfo(
    

    val isAutofillByApiEnabled: Boolean,

    

    val isAutofillByAccessibilityEnabled: Boolean,

    

    val isAutofillInKeyboardEnabled: Boolean,

    

    val manufacturer: String,

    

    val model: String,

    

    val keyboard: Pair<MonitoredApp.Keyboard, String>? = null,

    

    val browser: Pair<MonitoredApp.Browser, String>? = null,

    

    val browserMatchAutofillConfiguration: Boolean? = null
)
