package com.dashlane.ui

import android.app.Activity
import android.view.View
import android.view.Window

fun Activity.disableAutoFill() = this.window.disableAutoFill()

private fun Window.disableAutoFill() {
    decorView.importantForAutofill = View.IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS
}