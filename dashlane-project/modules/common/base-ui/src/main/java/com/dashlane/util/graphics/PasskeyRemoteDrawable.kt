package com.dashlane.util.graphics

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat.getColor
import androidx.core.graphics.drawable.DrawableCompat
import com.dashlane.design.iconography.IconTokens
import com.dashlane.ui.R

class PasskeyRemoteDrawable(
    private val context: Context,
    @ColorInt backgroundColor: Int,
) : CredentialRemoteDrawable(context, backgroundColor) {

    private val passkeyDrawable = AppCompatResources.getDrawable(context, IconTokens.passkeyOutlined.resource)

    override var image: Drawable?
        get() = super.image
        set(value) {
            super.setImage(value, true)
        }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)

        
        if (passkeyDrawable != null) {
            DrawableCompat.setTint(passkeyDrawable.mutate(), getColor(context, R.color.text_inverse_catchy))
            drawBottomRightIcon(canvas, bounds, passkeyDrawable)
        }
    }
}