package com.dashlane.util

import android.content.res.Resources
import android.text.Spannable
import android.text.SpannableStringBuilder
import java.util.regex.Pattern



private val pattern: Pattern by lazy { Pattern.compile("%1\\\$(?:\\.\\d+)?[a-z]") }



fun Resources.getFormattedSpannable(resId: Int, formatValue: Any, spanList: List<Any>):
        SpannableStringBuilder {
    val nonFormatted = getString(resId)
    val formatted = nonFormatted.format(formatValue)

    return SpannableStringBuilder(formatted).apply {
        val matcher = pattern.matcher(nonFormatted)
        if (matcher.find()) {
            val startingIndex = matcher.start()
            val endingIndex = matcher.end()
            val lengthDiff = formatted.length - nonFormatted.length

            spanList.forEach { span ->
                setSpan(span, startingIndex, endingIndex + lengthDiff, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }
    }
}
