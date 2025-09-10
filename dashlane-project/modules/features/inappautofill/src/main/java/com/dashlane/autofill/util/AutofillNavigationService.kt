package com.dashlane.autofill.util

import android.app.Activity
import android.app.PendingIntent
import android.content.IntentSender

interface AutofillNavigationService {
    fun navigateToAutofillSettings(activity: Activity, startAsNewTask: Boolean = false)

    fun navigateToPasswordSection(activity: Activity, startAsNewTask: Boolean = false)

    fun navigateToFrozenAccountPaywall(activity: Activity)

    fun getLongPressActionOnInline(): PendingIntent

    fun getOnBoardingIntentSender(): IntentSender

    companion object {
        const val REQUEST_KEYBOARD_AUTOFILL_ON_BOARDING = "requestKeyboardAutofillOnBoarding"
    }
}