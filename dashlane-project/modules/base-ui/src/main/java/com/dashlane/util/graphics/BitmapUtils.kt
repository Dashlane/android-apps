@file:JvmName("BitmapUtils")

package com.dashlane.util.graphics

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.SparseIntArray
import androidx.annotation.ColorInt

fun shouldDarkenColor(color: Int): Boolean {
    return getBrightness(color) >= 200
}

fun getBrightness(color: Int): Int {
    if (Color.TRANSPARENT == color) return 255
    val rgb = intArrayOf(Color.red(color), Color.green(color), Color.blue(color))
    return Math.sqrt(
        rgb[0].toDouble() * rgb[0].toDouble() * .241 +
                rgb[1].toDouble() * rgb[1].toDouble() * .691 +
                rgb[2].toDouble() * rgb[2].toDouble() * .068
    ).toInt()
}

@Suppress("DEPRECATION")
@ColorInt
fun getDominantColorFromBorder(drawable: Drawable): Int {
    if (drawable is BackgroundColorDrawable) {
        return drawable.backgroundColor
    }
    val b = Bitmap.createBitmap(20, 20, Bitmap.Config.ARGB_4444)
    drawable.setBounds(0, 0, b.width, b.height)
    drawable.draw(Canvas(b))

    val dominantColorFromBorder = getDominantColorFromBorder(b)
    b.recycle()
    return dominantColorFromBorder
}

@ColorInt
fun getDominantColorFromBorder(b: Bitmap): Int {
    check(!b.isRecycled) { "Attempting to get pixels of a recycled bitmap" }

    val width = b.width
    val height = b.height

    val buffer = IntArray(width * 2 + (height - 2) * 2) 
    var i = 0
    for (x in 0 until width) {
        buffer[i++] = b.getPixel(x, 0)
        buffer[i++] = b.getPixel(x, height - 1)
    }
    for (y in 1 until height - 1) { 
        buffer[i++] = b.getPixel(0, y)
        buffer[i++] = b.getPixel(width - 1, y)
    }
    return getDominantColor(buffer, false)
}

@Suppress("DEPRECATION")
@ColorInt
fun getDominantColor(drawable: Drawable): Int {
    if (drawable is BackgroundColorDrawable) {
        return drawable.backgroundColor
    }

    val b = Bitmap.createBitmap(20, 20, Bitmap.Config.ARGB_4444)
    drawable.setBounds(0, 0, b.width, b.height)
    drawable.draw(Canvas(b))

    val width = b.width
    val height = b.height

    val buffer = IntArray(width * height)
    b.getPixels(buffer, 0, width, 0, 0, width, height)
    b.recycle()

    return getDominantColor(buffer, true)
}

private fun getDominantColor(pixels: IntArray, ignoreConstantColors: Boolean): Int {
    val countPerColor = SparseIntArray(pixels.size / 4)
    var maxCount = 0
    var dominantColor = 0
    val hsv = FloatArray(3)
    for (i in pixels.indices) {
        val pixelColor = pixels[i]
        if (ignoreConstantColors) {
            Color.colorToHSV(pixelColor, hsv)
            if (isIgnoredColor(hsv)) {
                continue
            }
        }
        val approximatePixelColor = approximateColor(pixelColor)
        val newCount = countPerColor.get(approximatePixelColor) + 1
        countPerColor.put(approximatePixelColor, newCount)
        if (newCount > maxCount) {
            maxCount = newCount
            dominantColor = pixelColor
        }
    }
    return dominantColor
}

private fun isIgnoredColor(hsv: FloatArray): Boolean {
    return hsv[1] < 0.05 && hsv[2] >= 0.95 || hsv[2] <= 0.1 
}

private fun approximateColor(pixelColor: Int): Int {
    val red = Color.red(pixelColor)
    val green = Color.green(pixelColor)
    val blue = Color.blue(pixelColor)
    return Color.argb(255, roundToDecade(red), roundToDecade(green), roundToDecade(blue))
}

private fun roundToDecade(value: Int): Int {
    return Math.round(value / 10f) * 10
}

fun darkenColorByAmount(color: Int, amount: Float): Int {
    val red = (Color.red(color) * (1 - amount) / 255 * 255).toInt()
    val green = (Color.green(color) * (1 - amount) / 255 * 255).toInt()
    val blue = (Color.blue(color) * (1 - amount) / 255 * 255).toInt()
    return Color.rgb(red, green, blue)
}

interface BackgroundColorDrawable {
    @get:ColorInt
    @setparam:ColorInt
    var backgroundColor: Int
}