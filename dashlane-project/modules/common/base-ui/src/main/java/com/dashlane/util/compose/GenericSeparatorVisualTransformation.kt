package com.dashlane.util.compose

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

class GenericSeparatorVisualTransformation(private val dashIndex: Int, private val separator: Char) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val formatted = text
            .filter { it != separator }
            .chunked(dashIndex)
            .joinToString(separator.toString())

        val dashedOffsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                
                val existingSeparator = text.subSequence(0, offset).count { it == separator }
                val transformedOffsets = formatted
                    .mapIndexedNotNull { index, char -> index.takeIf { char != separator }?.plus(1) }
                    
                    .let { offsetList -> listOf(0) + offsetList }
                return transformedOffsets[offset] - existingSeparator
            }

            override fun transformedToOriginal(offset: Int): Int {
                return formatted
                    
                    .mapIndexedNotNull { index, c -> index.takeIf { c == separator } }
                    
                    .count { separatorIndex -> separatorIndex < offset }
                    
                    .let { separatorCount -> offset - separatorCount }
            }
        }

        return TransformedText(
            text = AnnotatedString(formatted),
            offsetMapping = dashedOffsetMapping
        )
    }
}
