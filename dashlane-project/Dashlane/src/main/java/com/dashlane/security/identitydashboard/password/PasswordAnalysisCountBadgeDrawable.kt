package com.dashlane.security.identitydashboard.password

import android.content.Context
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import android.graphics.drawable.StateListDrawable
import android.text.TextPaint
import android.util.StateSet
import android.util.TypedValue
import androidx.core.graphics.toRectF
import com.dashlane.R
import kotlin.math.max
import kotlin.math.roundToInt



class PasswordAnalysisCountBadgeDrawable(
    context: Context,
    indicator: Int,
    private val highlight: Boolean
) : Drawable() {

    private val textPaint = TextPaint().apply {
        textSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP, 12F,
            context.resources.displayMetrics
        )
        color = context.getColor(
            if (highlight) {
                R.color.text_danger_standard
            } else {
                R.color.text_neutral_standard
            }
        )
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
    }
    private val backgroundPaint = Paint().apply {
        isAntiAlias = true
        color = if (highlight) {
            context.getColor(R.color.container_expressive_danger_quiet_idle)
        } else {
            context.getColor(R.color.container_expressive_neutral_quiet_idle)
        }
        style = Paint.Style.FILL
    }
    private val textToDisplay = indicator.toString()
    private val textWidth = textPaint.measureText(textToDisplay)
    private val textMarginTopBottom = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, 3F,
        context.resources.displayMetrics
    )
    private val textMarginLeftRight = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, 6F,
        context.resources.displayMetrics
    )

    override fun draw(canvas: Canvas) {
        val radius = bounds.height().toFloat()
        canvas.drawRoundRect(bounds.toRectF(), radius, radius, backgroundPaint)

        val centerTextY = bounds.exactCenterY() - ((textPaint.descent() + textPaint.ascent()) / 2)
        canvas.drawText(textToDisplay, bounds.exactCenterX(), centerTextY, textPaint)
    }

    override fun setAlpha(alpha: Int) {
    }

    override fun getOpacity() = PixelFormat.OPAQUE

    override fun setColorFilter(colorFilter: ColorFilter?) {
    }

    override fun getIntrinsicHeight(): Int {
        return (textPaint.textSize + textMarginTopBottom * 2).toInt()
    }

    override fun getIntrinsicWidth(): Int {
        return max(intrinsicHeight, (textWidth + textMarginLeftRight * 2).roundToInt())
    }

    companion object {

        fun newStateListDrawable(context: Context, indicator: Int): Drawable {
            return StateListDrawable().apply {
                val regularDrawable = PasswordAnalysisCountBadgeDrawable(context, indicator, false)
                val highlightDrawable = PasswordAnalysisCountBadgeDrawable(context, indicator, true)

                addState(intArrayOf(android.R.attr.state_pressed), highlightDrawable)
                addState(intArrayOf(android.R.attr.state_selected), highlightDrawable)
                addState(StateSet.WILD_CARD, regularDrawable)
            }
        }
    }
}