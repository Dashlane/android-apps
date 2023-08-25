package com.dashlane.util.colorpassword

import android.content.Context
import android.text.Editable
import android.text.Spannable
import android.text.TextWatcher

open class ColorTextWatcher(context: Context) : TextWatcher {
    private val characterColor: CharacterColor = CharacterColor(context)

    override fun beforeTextChanged(charSequence: CharSequence, start: Int, before: Int, count: Int) = Unit

    override fun afterTextChanged(editable: Editable) = Unit

    override fun onTextChanged(charSequence: CharSequence, start: Int, before: Int, count: Int) {
        if (charSequence !is Spannable) return
        for (x in start until start + count) {
            characterColor.setColorForIndex(charSequence, x)
        }
    }
}