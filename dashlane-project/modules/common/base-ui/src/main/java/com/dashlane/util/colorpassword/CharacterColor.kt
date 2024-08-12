package com.dashlane.util.colorpassword

import android.content.Context
import android.text.Spannable
import android.text.style.ForegroundColorSpan
import com.dashlane.ui.R
import kotlin.math.min

class CharacterColor(private val context: Context) {
    fun setColorForIndex(charSequence: Spannable, index: Int) {
        getColor(charSequence[index])?.run {
            charSequence.setSpan(
                ForegroundColorSpan(this),
                index,
                min(index + 1, charSequence.length),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    }

    private fun getColor(char: Char): Int {
        return when {
            Character.isDigit(char) -> getColorDigit(context)
            Character.isLetter(char) -> getColorLetter(context)
            else -> getColorSymbol(context)
        }
    }

    private fun getColorDigit(context: Context): Int = context.getColor(R.color.text_oddity_password_digits)

    private fun getColorSymbol(context: Context): Int = context.getColor(R.color.text_oddity_password_symbols)

    private fun getColorLetter(context: Context): Int = context.getColor(R.color.text_neutral_catchy)
}