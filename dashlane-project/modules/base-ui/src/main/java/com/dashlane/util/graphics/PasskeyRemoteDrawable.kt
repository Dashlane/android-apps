package com.dashlane.util.graphics

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import androidx.appcompat.content.res.AppCompatResources
import com.dashlane.ui.R

class PasskeyRemoteDrawable(
    context: Context,
    @ColorInt backgroundColor: Int,
) : CredentialRemoteDrawable(context, backgroundColor) {

    private val passkeyDrawable = AppCompatResources.getDrawable(context, R.drawable.ic_vault_passkey)

    override var image: Drawable?
        get() = super.image
        set(value) {
            super.setImage(value, true)
        }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)

        
        if (passkeyDrawable != null) {
            drawBottomRightIcon(canvas, bounds, passkeyDrawable)
        }
    }
}