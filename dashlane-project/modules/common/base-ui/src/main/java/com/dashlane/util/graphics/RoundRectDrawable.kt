package com.dashlane.util.graphics

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.Drawable
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.ColorUtils
import com.dashlane.ui.R
import com.dashlane.util.hasGoodEnoughContrast
import kotlin.math.roundToInt

open class RoundRectDrawable(context: Context, backgroundColor: Int) : Drawable(), BackgroundColorDrawable {
    private val resources = context.resources
    private val clipPath = Path()
    private val backgroundPaint: Paint
    private val strokePaint: Paint
    private val width: Int = resources.getDimensionPixelSize(R.dimen.material_splash_bitmap_width)
    private val height: Int = resources.getDimensionPixelSize(R.dimen.material_splash_bitmap_height)
    private var drawableUsed: Drawable? = null
    private var withBorder = true
    var preferImageBackgroundColor = false

    final override var backgroundColor: Int
        get() = backgroundPaint.color
        set(backgroundColor) {
            backgroundPaint.color = backgroundColor
            val strokeColor: Int = if (hasGoodEnoughContrast(Color.BLACK, backgroundColor)) {
                Color.BLACK
            } else {
                Color.WHITE
            }
            invalidateSelf()
        }

    var isWithBorder: Boolean
        get() = withBorder
        set(withBorder) {
            this.withBorder = withBorder
            invalidateSelf()
        }

    open var image: Drawable?
        get() = drawableUsed
        set(drawable) {
            setImage(drawable, preferImageBackgroundColor)
        }

    init {
        val borderSize = resources.getDimensionPixelSize(R.dimen.default_rounded_bitmap_border_size)
        backgroundPaint = Paint()
        backgroundPaint.isAntiAlias = true
        backgroundPaint.style = Paint.Style.FILL
        strokePaint = Paint()
        strokePaint.isAntiAlias = true
        strokePaint.style = Paint.Style.STROKE
        strokePaint.strokeWidth = borderSize.toFloat()
        this.backgroundColor = backgroundColor
    }

    override fun draw(canvas: Canvas) {
        val tempRect = RectF()
        val halfStrokeWidth = strokePaint.strokeWidth / 2
        tempRect.left = bounds.left + halfStrokeWidth
        tempRect.top = bounds.top + halfStrokeWidth
        tempRect.right = bounds.right - halfStrokeWidth
        tempRect.bottom = bounds.bottom - halfStrokeWidth
        val cornerRadius = (bounds.height() * ROUND_CORNER_SIZE_RATIO).roundToInt()

        
        drawBackground(canvas, tempRect, bounds, backgroundPaint)

        
        clipPath.reset()
        clipPath.addRoundRect(tempRect, cornerRadius.toFloat(), cornerRadius.toFloat(), Path.Direction.CW)
        val saveCount = canvas.save()
        try {
            canvas.clipPath(clipPath)
        } catch (ex: UnsupportedOperationException) {
            
        }
        drawableUsed?.let {
            drawIcon(canvas, it)
        }
        canvas.restoreToCount(saveCount)

        
        if (withBorder) {
            canvas.drawRoundRect(tempRect, cornerRadius.toFloat(), cornerRadius.toFloat(), strokePaint)
        }
    }

    fun setImage(drawable: Drawable?, useImageDominantColor: Boolean) {
        preferImageBackgroundColor = useImageDominantColor
        drawableUsed = drawable
        if (preferImageBackgroundColor && drawable != null) {
            backgroundColor = getDominantColorFromBorder(drawable)
        }
        invalidateSelf()
    }

    override fun setAlpha(alpha: Int) {
        backgroundPaint.alpha = alpha
        strokePaint.alpha = alpha
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        backgroundPaint.setColorFilter(colorFilter)
        strokePaint.setColorFilter(colorFilter)
    }

    @Deprecated("Deprecated in Java")
    override fun getOpacity(): Int = backgroundPaint.alpha

    override fun getIntrinsicHeight(): Int = height

    override fun getIntrinsicWidth(): Int = width

    private fun drawBackground(canvas: Canvas, rectF: RectF?, bounds: Rect, paint: Paint?) {
        val cornerRadius = (bounds.height() * ROUND_CORNER_SIZE_RATIO).roundToInt()
        canvas.drawRoundRect(rectF!!, cornerRadius.toFloat(), cornerRadius.toFloat(), paint!!)
    }

    private fun drawIcon(canvas: Canvas, drawable: Drawable) {
        val bgBounds = bounds
        val intrinsicHeight = drawable.intrinsicHeight
        val intrinsicWidth = drawable.intrinsicWidth
        if (intrinsicWidth <= 0 || intrinsicHeight <= 0) {
            drawable.bounds = bgBounds
            drawable.draw(canvas)
            return
        }
        val ratio = intrinsicWidth / intrinsicHeight.toFloat()
        val bgRatio = bgBounds.width() / bgBounds.height().toFloat()
        if (ratio > bgRatio) {
            
            val newHeight = bgBounds.width() / ratio
            val top = (bgBounds.exactCenterY() - newHeight / 2f).roundToInt()
            val bottom = top + newHeight.roundToInt()
            drawable.setBounds(bgBounds.left, top, bgBounds.right, bottom)
        } else {
            val newWidth = ratio * bgBounds.height()
            val left = (bgBounds.exactCenterX() - newWidth / 2f).roundToInt()
            val right = left + newWidth.roundToInt()
            drawable.setBounds(left, bgBounds.top, right, bgBounds.bottom)
        }
        drawable.draw(canvas)
    }

    companion object {
        const val ROUND_CORNER_SIZE_RATIO = 6 / 120f 
        fun newWithImage(context: Context, backgroundColor: Int, imageOverResId: Int): RoundRectDrawable {
            val roundRectDrawable = RoundRectDrawable(context, backgroundColor)
            val secureNoteDrawable = AppCompatResources.getDrawable(context, imageOverResId)
            roundRectDrawable.setImage(secureNoteDrawable, false)
            return roundRectDrawable
        }
    }
}
