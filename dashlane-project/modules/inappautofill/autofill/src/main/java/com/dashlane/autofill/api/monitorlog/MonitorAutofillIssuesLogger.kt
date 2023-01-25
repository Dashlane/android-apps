package com.dashlane.autofill.api.monitorlog



interface MonitorAutofillIssuesLogger {
    fun logAutofillDeviceInfo(autofillStakeholdersInfo: AutofillStakeholdersInfo)
}