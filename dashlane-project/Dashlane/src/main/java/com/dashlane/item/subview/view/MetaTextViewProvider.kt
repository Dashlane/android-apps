package com.dashlane.item.subview.view

import android.content.Context
import android.view.ViewGroup.LayoutParams
import android.widget.LinearLayout
import android.widget.Space
import android.widget.TextView
import androidx.core.view.updatePaddingRelative
import com.dashlane.R
import com.dashlane.util.dpToPx
import com.dashlane.util.getThemeAttrColor
import com.dashlane.util.getThemeAttrResourceId
import kotlin.math.roundToInt



object MetaTextViewProvider {

    fun create(context: Context, header: String, value: String): LinearLayout {
        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            val horizontalSpacing = context.dpToPx(3.75f).roundToInt()
            updatePaddingRelative(start = horizontalSpacing, end = horizontalSpacing)
            addView(createHeaderView(context, header), newParams())
            addSpace(sizeInDp = 6.5f)
            addView(createDisabledTextView(context, value), newParams())
            addSpace(sizeInDp = 11f)
        }
    }

    private fun newParams() =
        LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)

    private fun createHeaderView(context: Context, header: String) =
        TextView(context).apply {
            text = header
            setTextAppearance(context.getThemeAttrResourceId(R.attr.textAppearanceBody2))
            setTextColor(context.getThemeAttrColor(R.attr.colorOnBackgroundMedium))
            
            includeFontPadding = false
        }

    private fun createDisabledTextView(context: Context, value: String) =
        TextView(context).apply {
            text = value
            setTextAppearance(context.getThemeAttrResourceId(R.attr.textAppearanceBody1))
            setTextColor(context.getColor(R.color.text_neutral_quiet))
        }

    private fun LinearLayout.addSpace(sizeInDp: Float) {
        val sizeInPx = context.dpToPx(sizeInDp).roundToInt()
        addView(Space(context), LayoutParams(sizeInPx, sizeInPx))
    }
}