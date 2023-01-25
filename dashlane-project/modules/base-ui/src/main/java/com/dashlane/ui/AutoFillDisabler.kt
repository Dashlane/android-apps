package com.dashlane.ui

import android.app.Activity
import android.os.Build
import android.view.View
import android.view.Window

fun Activity.disableAutoFill() {
    window?.disableAutoFill()
}

fun Window.disableAutoFill() {
    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
        decorView.importantForAutofill = View.IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS
    }
}