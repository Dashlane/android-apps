package com.dashlane.ui.widgets

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Outline
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.drawable.shapes.Shape
import android.os.Build



class TooltipPointShape(
    private val color: Int = Color.WHITE
) : Shape() {

    private val rect = Rect()
    private val path = Path()

    override fun draw(canvas: Canvas, paint: Paint) {
        computePath()

        paint.color = color
        canvas.drawPath(path, paint)
    }

    override fun onResize(width: Float, height: Float) {
        rect.right = width.toInt()
        rect.bottom = height.toInt()
    }

    override fun getOutline(outline: Outline) {
        computePath()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            outline.setPath(path)
        } else {
            @Suppress("DEPRECATION")
            outline.setConvexPath(path)
        }
    }

    private fun computePath() {
        val right = rect.right.toFloat()
        val top = rect.top.toFloat()
        val bottom = rect.bottom.toFloat()
        val left = rect.left.toFloat()

        path.apply {
            reset()
            moveTo(0.0f, 0.0f)
            lineTo(right, top)
            lineTo((right - left) / 2, (bottom - top) / 2)
            lineTo(0.0f, 0.0f)
            close()
        }
    }
}