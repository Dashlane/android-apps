package com.dashlane.autofillapi

import android.content.Intent
import android.content.Intent.ACTION_APPLICATION_PREFERENCES
import android.content.Intent.CATEGORY_APP_BROWSER
import android.content.Intent.CATEGORY_DEFAULT
import android.content.Intent.CATEGORY_PREFERENCE
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK

object ChromeAutofillSettingsHelper {
    fun buildChromeAutofillSettingsIntent(chromePackageName: String) = Intent(ACTION_APPLICATION_PREFERENCES).apply {
        addCategory(CATEGORY_DEFAULT)
        addCategory(CATEGORY_APP_BROWSER)
        addCategory(CATEGORY_PREFERENCE)
        setPackage(chromePackageName)
        flags = FLAG_ACTIVITY_NEW_TASK
    }
}