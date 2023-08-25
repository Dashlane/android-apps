package com.dashlane.item.subview.view

import android.animation.LayoutTransition
import android.content.Context
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import com.dashlane.R
import com.dashlane.util.dpToPx
import com.google.android.material.textfield.TextInputLayout

object PasswordWithStrengthViewProvider {

    fun create(context: Context, textInputLayout: TextInputLayout): LinearLayout {
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            
            layoutTransition = LayoutTransition()
            layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
        }
        textInputLayout.id = R.id.item_subview_text_input_layout
        layout.addView(textInputLayout)

        val progressBar = ProgressBar(context, null, -1, R.style.Widget_AppCompat_ProgressBar_Horizontal).apply {
            id = R.id.item_subview_strength_level_progress_bar
            isIndeterminate = false
            max = 100
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                context.dpToPx(4f).toInt()
            ).apply {
                val horizontalMargin = context.dpToPx(4f).toInt()
                topMargin = -context.dpToPx(10f).toInt()
                marginStart = horizontalMargin
                marginEnd = horizontalMargin
            }
        }
        layout.addView(progressBar)

        val strengthText = TextView(context).apply {
            id = R.id.item_subview_strength_level_textview
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                marginStart = context.dpToPx(6f).toInt()
            }
        }
        layout.addView(strengthText)

        return layout
    }
}