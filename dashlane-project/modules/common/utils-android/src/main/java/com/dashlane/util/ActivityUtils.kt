@file:JvmName("ActivityUtils")

package com.dashlane.util

import android.app.Activity
import android.view.ViewGroup
import android.view.WindowManager

fun Activity.hideSoftKeyboard() {
    val view = currentFocus
    if (view != null) {
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
}

fun Activity.findContentParent(): ViewGroup = findViewById(android.R.id.content)!!