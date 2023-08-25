package com.dashlane.ui.widgets

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.ShapeDrawable
import android.util.AttributeSet
import android.widget.LinearLayout
import com.dashlane.R
import com.dashlane.util.dpToPx
import com.dashlane.util.getThemeAttrColor
import kotlin.math.roundToInt

class BubbleLinearLayout : LinearLayout {

    private val drawable = ShapeDrawable(TooltipPointShape(context.getThemeAttrColor(R.attr.colorSurface)))

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(
        context,
        attrs,
        defStyleAttr,
        defStyleRes
    )

    init {
        setBackgroundResource(R.drawable.bg_card_bottom_margin_8dp)
        setWillNotDraw(true)

        val arrowHeight = context.dpToPx(16F).roundToInt()
        setPadding(
            paddingLeft,
            paddingTop,
            paddingRight,
            paddingBottom + arrowHeight
        )
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)

        val arrowSize = paddingBottom - paddingTop
        if (arrowSize <= 0) return 

        drawable.setBounds(arrowSize, bottom - arrowSize, arrowSize + arrowSize, bottom)
        drawable.draw(canvas)
    }
}