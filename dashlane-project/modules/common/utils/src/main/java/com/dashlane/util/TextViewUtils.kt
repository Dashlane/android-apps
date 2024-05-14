package com.dashlane.util

import android.text.Editable
import android.text.TextWatcher
import android.widget.TextView

fun TextView.addTextChangedListener(block: TextWatcherDsl.() -> Unit): TextWatcher =
    TextWatcherImpl().also { block(it); addTextChangedListener(it) }

interface TextWatcherDsl {
    fun beforeTextChanged(beforeTextChanged: (CharSequence, Int, Int, Int) -> Unit)
    fun onTextChanged(onTextChanged: (CharSequence, Int, Int, Int) -> Unit)
    fun afterTextChanged(afterTextChanged: (Editable) -> Unit)
}

private class TextWatcherImpl : TextWatcher, TextWatcherDsl {

    private var beforeTextChanged: ((CharSequence, Int, Int, Int) -> Unit)? = null
    private var onTextChanged: ((CharSequence, Int, Int, Int) -> Unit)? = null
    private var afterTextChanged: ((Editable) -> Unit)? = null

    override fun beforeTextChanged(beforeTextChanged: (CharSequence, Int, Int, Int) -> Unit) {
        this.beforeTextChanged = beforeTextChanged
    }

    override fun onTextChanged(onTextChanged: (CharSequence, Int, Int, Int) -> Unit) {
        this.onTextChanged = onTextChanged
    }

    override fun afterTextChanged(afterTextChanged: (Editable) -> Unit) {
        this.afterTextChanged = afterTextChanged
    }

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
        beforeTextChanged?.invoke(s, start, count, after)
    }

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        onTextChanged?.invoke(s, start, before, count)
    }

    override fun afterTextChanged(s: Editable) {
        afterTextChanged?.invoke(s)
    }
}

fun TextView.removeMaxLines() {
    maxLines = Int.MAX_VALUE
}