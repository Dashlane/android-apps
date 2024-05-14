package com.dashlane.ui.activities.fragments.checklist

import java.time.Instant

interface ChecklistDataProviderContract {
    fun hasDarkWebMonitoring(): Boolean
    fun hasAddedAccounts(): Boolean
    fun hasFinishedM2D(): Boolean
    fun hasCompletedAndAcknowledgedAddCredential(): Boolean
    fun hasCompletedAndAcknowledgedAutofillActivation(): Boolean
    fun hasCompletedAndAcknowledgedM2D(): Boolean
    fun saveAddCredentialCompletedAndAcknowledged(): Boolean
    fun saveAutofillActiationCompletedAndAcknowledgedAutofill(): Boolean
    fun saveM2DCompletedAndAcknowledged(): Boolean
    fun saveDarkWebMonitoringCompletedAndAcknowledged(): Boolean
    fun getGuidedOnBoardingAction(): Int
    fun hasSeenChecklist(): Boolean
    fun setChecklistSeen()
    fun setChecklistDismissed()
    fun getAccountCreationDate(): Instant
    fun hasActivatedAutofill(): Boolean

    fun hasCompletedAndAcknowledgedDarkWebAlerts(): Boolean
}