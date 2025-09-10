package com.dashlane.item.v3.itemtype.creditcard.ui

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import com.dashlane.design.component.ObfuscatedVisualTransformation

fun paymentFormatCardNumberTransformation(format: String) = { value: String? ->
    value?.let {
        ObfuscatedVisualTransformation(
            hidden = CreditCardVisualTransformation.hidden(format),
            revealed = CreditCardVisualTransformation.revealed(format),
        ).revealed
            .filter(AnnotatedString(value)).text.toString()
    }
}

class CreditCardVisualTransformation private constructor(
    private val format: String,
    private val reveal: Boolean,
) : VisualTransformation {
    init {
        require(format.all { it == CHAR || it == SPACE }) {
            "Credit card format must contain only '$CHAR' or '$SPACE' characters"
        }
    }

    override fun filter(text: AnnotatedString): TransformedText {
        val formattedText = StringBuilder()
        var index = 0
        format.forEach { char ->
            if (char == CHAR && index < text.length) {
                if (reveal) {
                    formattedText.append(text[index])
                } else {
                    formattedText.append(OBFUSCATED_MASK)
                }
                index++
            } else if (char == SPACE && index < text.length) {
                formattedText.append(SPACE)
            }
        }
        if (index < text.length) {
            if (reveal) {
                formattedText.append(text.substring(index))
            } else {
                formattedText.append(OBFUSCATED_MASK.repeat(text.length - index))
            }
        }

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val spaces = formattedText.count { it == ' ' }
                return offset + spaces
            }

            override fun transformedToOriginal(offset: Int): Int {
                val spaces = formattedText.substring(0, offset).count { it == ' ' }
                return offset - spaces
            }
        }

        return TransformedText(AnnotatedString(formattedText.toString()), offsetMapping)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CreditCardVisualTransformation) return false
        if (reveal != other.reveal) return false
        return true
    }

    override fun hashCode(): Int {
        return reveal.hashCode()
    }

    companion object {
        fun revealed(format: String) = CreditCardVisualTransformation(
            format = format,
            reveal = true,
        )

        fun hidden(format: String) = CreditCardVisualTransformation(
            format = format,
            reveal = false,
        )

        private const val OBFUSCATED_MASK = "\u2022"
        private const val SPACE = ' '
        private const val CHAR = '#'
    }
}