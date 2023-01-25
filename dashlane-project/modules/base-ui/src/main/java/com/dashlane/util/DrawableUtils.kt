package com.dashlane.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import com.dashlane.ui.drawable.PlaceholderForTextDrawableFactory
import com.dashlane.util.graphics.RemoteImageRoundRectDrawable

fun Drawable.toBitmap(): Bitmap = when {
    this is BitmapDrawable -> bitmap
    intrinsicWidth <= 0 || intrinsicHeight <= 0 -> Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
    else -> Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888).apply {
        val canvas = Canvas(this)
        setBounds(0, 0, canvas.width, canvas.height)
        draw(canvas)
    }
}



fun Context.getImageDrawableByWebsiteUrl(title: String?, websiteUrl: String?): Drawable {
    val drawable = RemoteImageRoundRectDrawable(
        this,
        this.getThemeAttrColor(R.attr.colorPrimary)
    )
    drawable.setPreferImageBackgroundColor(true)
    drawable.loadImage(websiteUrl, getPlaceholder(title))
    return drawable
}



fun Context.getImageDrawableByWebsiteUrl(title: String?): Drawable {
    return getImageDrawableByWebsiteUrl(title, title)
}



fun Context.getPlaceholder(originalTitle: String?): Drawable {
    return PlaceholderForTextDrawableFactory.buildDrawable(
        this,
        originalTitle,
        getThemeAttrColor(com.dashlane.ui.R.attr.colorOnPrimary),
        getThemeAttrColor(R.attr.colorPrimary)
    )
}
