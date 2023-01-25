package com.dashlane.util

import android.content.Context
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat



fun String.toForegroundColorSpan(@ColorInt color: Int): CharSequence {
    return SpannableString(this).apply {
        setSpan(
            ForegroundColorSpan(color),
            0, length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }
}



fun String.toForegroundColorSpan(context: Context, @ColorRes colorResId: Int): CharSequence {
    return toForegroundColorSpan(ContextCompat.getColor(context, colorResId))
}



fun Context.getStringFormatted(@StringRes textResId: Int, vararg args: CharSequence): CharSequence {
    val placeholders = args.mapIndexed { index, _ ->
        "%PLACEHOLDER_ARG_$index%"
    }
    val text = getString(textResId, *placeholders.toTypedArray())
    val indexesPlaceholders = placeholders.map { text.indexOf(it) }
    val indexesPlaceholdersSorted = indexesPlaceholders.sorted()

    return SpannableStringBuilder().apply {

        var previousTextIndex = 0
        var indexInSortedIndexes = 0
        while (indexInSortedIndexes < indexesPlaceholdersSorted.size) {
            val indexPlaceholder = indexesPlaceholdersSorted[indexInSortedIndexes++]
            val indexArg = indexesPlaceholders.indexOf(indexPlaceholder)
            val placeholder = placeholders[indexArg]
            val arg = args[indexArg]

            append(text.substring(previousTextIndex, indexPlaceholder))
            previousTextIndex = indexPlaceholder + placeholder.length
            append(arg)
        }
        append(text.substring(previousTextIndex))
    }
}



fun Spannable.setOnFirst(token: String, span: Any, flags: Int = Spannable.SPAN_INCLUSIVE_EXCLUSIVE) {
    val start = toString().indexOf(token)
    if (start != -1) {
        setSpan(span, start, start + token.length, flags)
    }
}

class OnClickSpan(private val operation: () -> Unit) : ClickableSpan() {
    override fun onClick(widget: View) {
        operation.invoke()
    }
}

fun Spannable.toHighlightedSpannable(
    targetText: String,
    @ColorInt highlightColor: Int,
    ignoreCase: Boolean = true
): Spannable {
    val startIndex = this.indexOf(string = targetText, ignoreCase = ignoreCase)
    if (startIndex >= 0) {
        this.setSpannableBoldColor(
            colorInt = highlightColor,
            start = startIndex,
            end = startIndex + targetText.length
        )
    }
    return this
}



fun Spannable.setSpannableBoldColor(@ColorInt colorInt: Int, start: Int, end: Int) {
    setSpan(StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    setSpan(ForegroundColorSpan(colorInt), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
}