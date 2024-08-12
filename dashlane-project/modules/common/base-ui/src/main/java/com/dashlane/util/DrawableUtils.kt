package com.dashlane.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable

fun Drawable.toBitmap(): Bitmap = when {
    this is BitmapDrawable -> bitmap
    intrinsicWidth <= 0 || intrinsicHeight <= 0 -> Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
    else -> Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888).apply {
        val canvas = Canvas(this)
        setBounds(0, 0, canvas.width, canvas.height)
        draw(canvas)
    }
}
