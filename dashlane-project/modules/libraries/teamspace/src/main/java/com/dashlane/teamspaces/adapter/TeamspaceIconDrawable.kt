package com.dashlane.teamspaces.adapter

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import androidx.annotation.ColorInt
import androidx.core.content.res.ResourcesCompat
import com.dashlane.ui.R
import com.dashlane.util.graphics.TextDrawable

class TeamspaceIconDrawable(
    context: Context,
    displayLetter: Char,
    @ColorInt color: Int
) : TextDrawable(
    displayLetter.toString(),
    Color.WHITE,
    color,
    ResourcesCompat.getFont(context, R.font.gt_walsheim_pro_bold)
) {
    private val path = Path()

    private val paintBorder = Paint()
    private val tempRect = Rect()
    var size = -1
        set(value) {
            field = value
            invalidateSelf()
        }

    init {
        paintBorder.color = Color.BLACK
        paintBorder.alpha = Math.round(0.24f * 256)
        paintBorder.isAntiAlias = true
        paintBorder.style = Paint.Style.STROKE
    }

    override fun drawBackground(canvas: Canvas, bounds: Rect, paint: Paint) {
        fillBackgroundPath(bounds, path)
        canvas.drawPath(path, paint)
        tempRect.set(bounds)
        val borderSize = Math.round(bounds.width().toFloat() * BORDER_SIZE_RATIO)
        val halfBorderSize = borderSize / 2f
        tempRect.bottom = (tempRect.bottom - halfBorderSize).toInt()
        tempRect.top = (tempRect.top + halfBorderSize).toInt()
        tempRect.right = (tempRect.right - halfBorderSize).toInt()
        tempRect.left = (tempRect.left + halfBorderSize).toInt()
        paintBorder.strokeWidth = borderSize.toFloat()
        fillBackgroundPath(tempRect, path)
        canvas.drawPath(path, paintBorder)
    }

    override fun getTextSizeFactor(): Float {
        return TEXT_SIZE_FACTOR
    }

    private fun fillBackgroundPath(bounds: Rect, fillPath: Path) {
        val width = bounds.width()
        val height = bounds.height()
        val thirdWidth = width / 3f
        val thirdHeight = height / 3f
        fillPath.reset()
        fillPath.moveTo(bounds.left + thirdWidth, bounds.top.toFloat())
        fillPath.lineTo(bounds.right.toFloat(), bounds.top.toFloat())
        fillPath.lineTo(bounds.right.toFloat(), bounds.bottom - thirdHeight)
        fillPath.lineTo(bounds.right - thirdWidth, bounds.bottom.toFloat())
        fillPath.lineTo(bounds.left.toFloat(), bounds.bottom.toFloat())
        fillPath.lineTo(bounds.left.toFloat(), bounds.top + thirdHeight)
        fillPath.close()
    }

    override fun getIntrinsicWidth(): Int {
        return size
    }

    override fun getIntrinsicHeight(): Int {
        return size
    }

    companion object {
        private const val TEXT_SIZE_FACTOR = 0.5f
        private const val BORDER_SIZE_RATIO = 2 / 24f 
    }
}
