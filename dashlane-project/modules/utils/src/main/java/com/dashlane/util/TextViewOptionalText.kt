package com.dashlane.util

import android.view.View
import android.widget.TextView
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class TextViewOptionalText(
    val textView: TextView,
    private val nullVisibility: Int,
    private val paintFlag: Int? = null
) : ReadWriteProperty<Any, CharSequence?> {
    override fun getValue(thisRef: Any, property: KProperty<*>): CharSequence? = textView.text
    override fun setValue(thisRef: Any, property: KProperty<*>, value: CharSequence?) {
        textView.text = value
        textView.visibility = if (value == null) nullVisibility else View.VISIBLE
        if (paintFlag != null) {
            textView.paintFlags = textView.paintFlags or paintFlag
        }
    }
}