package com.dashlane.ui.activities.fragments.checklist

interface ChecklistLoggerContract {
    fun logDisplay(dismissable: Boolean, hasDarkWeb: Boolean)
    fun logClickAddAccount()
    fun logClickActivateAutofill()
    fun logClickAddComputer()
    fun logClickDarkWebMonitoring()
    fun logClickDismiss()
}