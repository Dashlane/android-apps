package com.dashlane.ui.activities.intro

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout

internal class IntroButtonBarLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private var lastWidthSize = -1

    private var isStacked: Boolean
        get() = orientation == VERTICAL
        set(stacked) {
            orientation = if (stacked) VERTICAL else HORIZONTAL
            gravity = if (stacked) Gravity.END else Gravity.BOTTOM
        }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)

        if (widthSize > lastWidthSize && isStacked) {
            
            isStacked = false
        }

        lastWidthSize = widthSize

        var needsRemeasure = false

        
        
        
        val initialWidthMeasureSpec: Int
        if (!isStacked && MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.EXACTLY) {
            initialWidthMeasureSpec = MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.AT_MOST)

            
            needsRemeasure = true
        } else {
            initialWidthMeasureSpec = widthMeasureSpec
        }

        super.onMeasure(initialWidthMeasureSpec, heightMeasureSpec)

        if (!isStacked) {
            val measuredWidth = measuredWidthAndState
            val measuredWidthState = measuredWidth and View.MEASURED_STATE_MASK
            val stack = measuredWidthState == View.MEASURED_STATE_TOO_SMALL

            if (stack) {
                isStacked = true
                
                needsRemeasure = true
            }
        }

        if (needsRemeasure) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }
    }
}