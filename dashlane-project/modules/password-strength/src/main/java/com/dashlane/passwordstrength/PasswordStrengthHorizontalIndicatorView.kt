package com.dashlane.passwordstrength

import android.content.Context
import android.util.AttributeSet
import android.view.View



class PasswordStrengthHorizontalIndicatorView : View {

    var minHeight
        get() = indicatorDrawable.minHeight
        set(value) {
            indicatorDrawable.minHeight = value
        }

    var marginBetweenLevels
        get() = indicatorDrawable.marginBetweenLevels
        set(value) {
            indicatorDrawable.marginBetweenLevels = value
        }

    private val indicatorDrawable: PasswordStrengthHorizontalIndicatorDrawable =
        PasswordStrengthHorizontalIndicatorDrawable(context)

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
        background = indicatorDrawable
    }

    fun setPasswordStrength(passwordStrength: PasswordStrength?) {
        setPasswordStrength(passwordStrength?.score)
    }

    fun setPasswordStrength(passwordStrength: PasswordStrengthScore?) {
        indicatorDrawable.passwordStrength = passwordStrength
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(
            getBestSize(widthMeasureSpec, Integer.MAX_VALUE), 
            getBestSize(heightMeasureSpec, indicatorDrawable.minimumHeight)
        )
    }

    private fun getBestSize(measureSpec: Int, desiredSize: Int): Int {
        val specMode = View.MeasureSpec.getMode(measureSpec)
        val specSize = View.MeasureSpec.getSize(measureSpec)
        return when (specMode) {
            MeasureSpec.EXACTLY -> 
                specSize
            MeasureSpec.AT_MOST -> 
                Math.min(desiredSize, specSize)
            MeasureSpec.UNSPECIFIED -> 
                desiredSize
            else -> desiredSize
        }
    }
}