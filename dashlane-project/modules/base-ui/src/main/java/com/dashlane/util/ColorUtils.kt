package com.dashlane.util

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.widget.ImageView
import androidx.annotation.ColorInt
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.graphics.ColorUtils
import com.dashlane.ui.R

fun hasGoodEnoughContrast(
    @ColorInt foreground: Int,
    @ColorInt background: Int
) = ColorUtils.calculateContrast(foreground, ColorUtils.setAlphaComponent(background, 0xFF)) > 1.5

@ColorInt
fun Context.getColorOn(@ColorInt background: Int): Int =
    getColorToColorOns(this).firstOrNull { it.first == background }?.second
        ?: Color.WHITE.takeIf { hasGoodEnoughContrast(Color.WHITE, background) }
        ?: Color.BLACK

@JvmName("setToolbarContentTint")
fun Toolbar.setContentTint(@ColorInt tint: Int) {
    navigationIcon = navigationIcon?.mutate()?.also { it.setTint(tint) }
    overflowIcon = overflowIcon?.mutate()?.also { it.setTint(tint) }
    setTitleTextColor(tint)

    for (i in 0 until menu.size()) {
        val item = menu.getItem(i)
        item.icon = item.icon?.mutate()?.also { it.setTint(tint) }
        (item.actionView as? SearchView)?.setMagIconTint(tint)
    }
}

@JvmName("setSearchViewMagIconTint")
fun SearchView.setMagIconTint(@ColorInt tint: Int) {
    findViewById<ImageView>(R.id.search_mag_icon)?.setColorFilter(tint, PorterDuff.Mode.SRC_IN)
    findViewById<ImageView>(R.id.search_button)?.setColorFilter(tint, PorterDuff.Mode.SRC_IN)
}

private fun getColorToColorOns(context: Context) = sequenceOf(
    context.getThemeAttrColor(R.attr.colorPrimary) to context.getThemeAttrColor(R.attr.colorOnPrimary),
    context.getThemeAttrColor(R.attr.colorSurface) to context.getThemeAttrColor(R.attr.colorOnSurface),
    context.getColor(R.color.container_agnostic_neutral_standard) to context.getColor(R.color.text_neutral_standard)
)

private val toolbarForegroundColorsRes: Set<Int> = setOf(
    R.color.text_neutral_catchy,
    R.color.text_inverse_catchy
)

@ColorInt
fun Context.getColorOnForToolbar(@ColorInt background: Int) =
    toolbarForegroundColorsRes.asSequence()
        .map { getColor(it) }
        .firstOrNull { hasGoodEnoughContrast(foreground = it, background = background) }
        ?: Color.WHITE.takeIf { hasGoodEnoughContrast(Color.WHITE, background) }
        ?: Color.BLACK
