package com.dashlane.util

import android.content.Context
import android.graphics.Color
import android.text.Annotation
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannedString
import android.text.style.CharacterStyle
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
import androidx.annotation.StringRes



fun Context.getTextAnnotatedFormatted(
    @StringRes textResId: Int,
    vararg actions: TextAnnotationProcessor =
        getAllDashlaneTextAnnotationProcessors()
): CharSequence {

    val original = getText(textResId) as SpannedString
    val result = SpannableString(original)

    val annotations = original.getSpans(0, original.length, Annotation::class.java)
    annotations.forEach { annotation ->
        actions.forEach { textAnnotationProcessor ->
            textAnnotationProcessor.getSpan(annotation.key, annotation.value)
                ?.let {
                    result.setSpan(
                        it,
                        original.getSpanStart(annotation),
                        original.getSpanEnd(annotation),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
        }
    }
    return result
}

private fun getAllDashlaneTextAnnotationProcessors() =
    arrayOf(
        DashlaneColorTextAnnotationProcessor,
        UnderlineTextAnnotationProcessor
    )



interface TextAnnotationProcessor {

    fun getSpan(key: String, value: String): CharacterStyle?
}



object DashlaneColorTextAnnotationProcessor : TextAnnotationProcessor {

    override fun getSpan(key: String, value: String): CharacterStyle? {
        if (key != "color") return null
        val color = when (value) {
            "pure_blue" -> Color.BLUE
            "pure_red" -> Color.RED
            else -> null
        } ?: return null
        return ForegroundColorSpan(color)
    }
}



object UnderlineTextAnnotationProcessor : TextAnnotationProcessor {

    override fun getSpan(key: String, value: String): CharacterStyle? {
        if (key == "underline" &&
            value == "true"
        ) {
            return UnderlineSpan()
        }
        return null
    }
}
