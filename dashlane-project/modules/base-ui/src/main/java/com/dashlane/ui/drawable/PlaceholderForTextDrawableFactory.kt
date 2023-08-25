package com.dashlane.ui.drawable

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import androidx.core.content.res.ResourcesCompat
import com.dashlane.ui.R
import com.dashlane.url.root
import com.dashlane.url.toUrlOrNull
import com.dashlane.util.graphics.TextDrawable
import java.util.Locale

object PlaceholderForTextDrawableFactory {
    @JvmStatic
    fun buildDrawable(
        context: Context,
        originalTitle: String?,
        @ColorInt textColor: Int,
        @ColorInt backgroundColor: Int
    ): Drawable {
        var letters = "?"
        if (originalTitle != null) {
            var title = originalTitle.toUrlOrNull()?.root
            if (title == null) {
                title = originalTitle.trim { it <= ' ' }
            }
            if (title.startsWith("http")) {
                val firstDot = title.indexOf("
                if (firstDot > 0) {
                    title = title.substring(firstDot + 2)
                }
            }
            if (title.startsWith("www.")) {
                title = title.substring(4)
            }
            if (title.length >= 2) {
                letters =
                    title.get(0).toString().uppercase(Locale.US) + title.get(1).toString().lowercase(Locale.US)
            } else if (title.length == 1) {
                letters = title.uppercase(Locale.US)
            }
        }
        letters = letters.trim { it <= ' ' }
        if (letters.isEmpty()) {
            letters = "?"
        }
        return TextDrawable(letters, textColor, backgroundColor, ResourcesCompat.getFont(context, R.font.roboto_medium))
    }
}
