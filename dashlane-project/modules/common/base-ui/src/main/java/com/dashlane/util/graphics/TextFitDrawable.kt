package com.dashlane.util.graphics

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.text.TextPaint
import androidx.annotation.ColorInt
import androidx.annotation.Px

open class TextFitDrawable @JvmOverloads constructor(
    private val text: String,
    @ColorInt textColor: Int,
    @Px textSize: Float,
    @ColorInt backgroundColor: Int = Color.TRANSPARENT,
    typeface: Typeface? = null
) : Drawable(),
    BackgroundColorDrawable {
    private val backgroundPaint: Paint = Paint()
    private val textPaint: TextPaint = TextPaint()

    override var backgroundColor: Int
        @ColorInt
        get() = backgroundPaint.color
        set(@ColorInt color) {
            backgroundPaint.color = color
        }

    private val measuredTextWidth: Int
    private val measuredTextHeight: Int

    init {
        backgroundPaint.isAntiAlias = true
        backgroundPaint.style = Paint.Style.FILL
        backgroundPaint.color = backgroundColor

        textPaint.style = Paint.Style.FILL
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.isAntiAlias = true
        textPaint.color = textColor
        if (typeface != null) {
            textPaint.typeface = typeface
        }

        textPaint.textSize = textSize

        val intrinsicBounds = Rect()
        textPaint.getTextBounds(text, 0, text.length, intrinsicBounds)
        measuredTextWidth = intrinsicBounds.right - intrinsicBounds.left
        measuredTextHeight = intrinsicBounds.bottom - intrinsicBounds.top
    }

    override fun draw(canvas: Canvas) {
        val rect = bounds

        if (!text.isNullOrEmpty()) {
            val xPos = rect.exactCenterX()
            val yPos = rect.exactCenterY() - (textPaint.descent() + textPaint.ascent()) / 2f
            canvas.drawText(text, xPos, yPos, textPaint)
        }
    }

    override fun getIntrinsicHeight(): Int = measuredTextHeight

    override fun getIntrinsicWidth(): Int = measuredTextWidth

    override fun setAlpha(alpha: Int) {
        backgroundPaint.alpha = alpha
        textPaint.alpha = alpha
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        backgroundPaint.colorFilter = colorFilter
    }

    override fun getOpacity(): Int {
        return PixelFormat.UNKNOWN
    }
}
