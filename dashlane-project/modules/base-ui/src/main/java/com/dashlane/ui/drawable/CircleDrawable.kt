package com.dashlane.ui.drawable

import android.content.Context
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.graphics.drawable.InsetDrawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import com.dashlane.ui.R



object CircleDrawable {
    @JvmStatic
    fun with(
        context: Context,
        @ColorRes backgroundColorRes: Int,
        @DrawableRes drawableRes: Int,
        @ColorRes drawableTintColorRes: Int
    ): Drawable {
        val drawable = AppCompatResources.getDrawable(context, drawableRes)
        drawable?.setTint(context.getColor(drawableTintColorRes))
        return with(context, backgroundColorRes, drawable)
    }

    @JvmStatic
    fun with(context: Context, @ColorRes backgroundColorRes: Int, drawable: Drawable?): Drawable {

        val circleBackground = ShapeDrawable(OvalShape()).apply {
            paint.isAntiAlias = true
            paint.style = Paint.Style.FILL
            paint.color = context.getColor(backgroundColorRes)
        }

        val inset = context.resources.getDimensionPixelSize(R.dimen.circle_drawable_inset)
        val icon = InsetDrawable(drawable, inset)

        return LayerDrawable(arrayOf(circleBackground, icon))
    }
}