package com.dashlane.passwordstrength

import android.content.Context
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import com.dashlane.util.dpToPx
import kotlin.math.roundToInt

class PasswordStrengthHorizontalIndicatorDrawable(context: Context) : Drawable() {

    var passwordStrength: PasswordStrengthScore? = null
        set(value) {
            field = value
            invalidateSelf() 
        }
    var marginBetweenLevels = context.dpToPx(4F).roundToInt()
    var minHeight = context.dpToPx(4F).roundToInt()

    private val disabledBarColor = context.getColor(R.color.border_neutral_quiet_idle)
    private val barColors =
        enumValues<PasswordStrengthScore>().associateWith { context.getColor(it.borderColorRes) }

    private val paint = Paint().apply {
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    override fun draw(canvas: Canvas) {
        val passwordStrengths = PasswordStrengthScore.values()
        val barCount = passwordStrengths.size
        val bounds = bounds

        val singleBarWidth = (bounds.width() - (barCount - 1) * marginBetweenLevels) / barCount
        val radiusSize = bounds.height() / 2F

        var currentX = bounds.left
        passwordStrengths.forEach { passwordStrengthToDisplay ->

            paint.color = passwordStrength?.takeIf { it >= passwordStrengthToDisplay }
                ?.let { barColors[it] }
                ?: disabledBarColor

            canvas.drawRoundRect(
                currentX.toFloat(),
                bounds.top.toFloat(),
                (currentX + singleBarWidth).toFloat(),
                bounds.bottom.toFloat(),
                radiusSize,
                radiusSize,
                paint
            )

            currentX += singleBarWidth + marginBetweenLevels
        }
    }

    override fun setAlpha(alpha: Int) {
    }

    override fun getOpacity() = PixelFormat.TRANSLUCENT

    override fun setColorFilter(colorFilter: ColorFilter?) {
    }

    override fun getMinimumHeight(): Int {
        return minHeight
    }
}