package com.dashlane.ui.util

import android.content.Context
import android.view.Gravity
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DialogHelper @Inject constructor() {
    fun builder(context: Context): AlertDialog.Builder {
        return MaterialAlertDialogBuilder(context)
    }

    fun builder(
        context: Context,
        overrideThemeResId: Int
    ): AlertDialog.Builder {
        return MaterialAlertDialogBuilder(context, overrideThemeResId)
    }
}

fun AlertDialog.withCenteredButtons() {
    val positive = getButton(AlertDialog.BUTTON_POSITIVE)
    
    (positive.parent as? LinearLayout)?.apply { gravity = Gravity.CENTER_HORIZONTAL }
    
    (positive.layoutParams as? LinearLayout.LayoutParams)?.apply { gravity = Gravity.CENTER }
    val negative = getButton(AlertDialog.BUTTON_NEGATIVE)
    (negative.layoutParams as? LinearLayout.LayoutParams)?.apply { gravity = Gravity.CENTER }
    val neutral = getButton(AlertDialog.BUTTON_NEUTRAL)
    (neutral.layoutParams as? LinearLayout.LayoutParams)?.apply { gravity = Gravity.CENTER }
}