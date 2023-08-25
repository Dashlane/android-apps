package com.dashlane.autofill.api.util

import android.app.Activity
import android.app.PendingIntent
import android.content.IntentSender
import com.dashlane.autofill.formdetector.model.AutoFillFormSource

interface AutofillNavigationService {
    fun navigateToPlansPage(activity: Activity, origin: String)

    fun navigateToAutofillSettings(activity: Activity, startAsNewTask: Boolean = false)

    fun navigateToPasswordSection(activity: Activity, startAsNewTask: Boolean = false)

    fun navigateToPausedAutofillSection(
        activity: Activity,
        autofillFormSource: AutoFillFormSource,
        origin: String
    )

    fun getLongPressActionOnInline(): PendingIntent

    fun getOnBoardingIntentSender(): IntentSender

    companion object {
        const val ORIGIN_PASSWORD_LIMIT = "getPremium_PasswordLimit_Autofill"
        const val ORIGIN_OS_SETTINGS = "osSettings"
        const val REQUEST_KEYBOARD_AUTOFILL_ON_BOARDING = "requestKeyboardAutofillOnBoarding"
    }
}