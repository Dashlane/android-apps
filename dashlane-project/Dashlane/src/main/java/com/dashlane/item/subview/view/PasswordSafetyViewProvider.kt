package com.dashlane.item.subview.view

import android.content.Context
import android.widget.LinearLayout
import android.widget.TextView
import com.dashlane.R
import com.dashlane.util.dpToPx
import com.dashlane.util.getThemeAttrResourceId
import kotlin.math.roundToInt



object PasswordSafetyViewProvider {

    fun create(context: Context, header: String): LinearLayout {
        val startMargin = context.dpToPx(4f).toInt()
        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 0, 0, context.dpToPx(10f).roundToInt())
            addView(TextView(context).apply {
                id = R.id.password_safety_label_textview
                text = header
                setTextAppearance(context.getThemeAttrResourceId(R.attr.textAppearanceBody2))
                letterSpacing = 0f 
                setTextColor(context.getColor(R.color.text_neutral_quiet))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    bottomMargin = context.dpToPx(8f).roundToInt()
                    marginStart = startMargin
                }
            })
            addView(TextView(context).apply {
                id = R.id.password_safety_strength_textview
                setTextAppearance(context.getThemeAttrResourceId(R.attr.textAppearanceCaption))
                setTextColor(context.getColor(R.color.text_neutral_standard))
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                params.marginStart = startMargin
                layoutParams = params
            })
            addView(TextView(context).apply {
                id = R.id.password_safety_reused_textview
                setTextAppearance(context.getThemeAttrResourceId(R.attr.textAppearanceCaption))
                setTextColor(context.getColor(R.color.text_danger_standard))
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                params.marginStart = startMargin
                layoutParams = params
            })
            addView(TextView(context).apply {
                id = R.id.password_safety_compromised_textview
                setTextAppearance(context.getThemeAttrResourceId(R.attr.textAppearanceCaption))
                setTextColor(context.getColor(R.color.text_danger_standard))
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                params.marginStart = startMargin
                layoutParams = params
            })
        }
    }
}