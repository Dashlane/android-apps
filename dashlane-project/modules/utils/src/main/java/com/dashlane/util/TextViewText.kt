package com.dashlane.util

import android.widget.TextView
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty



class TextViewText(val textView: TextView) : ReadWriteProperty<Any, CharSequence> {
    override fun getValue(thisRef: Any, property: KProperty<*>): CharSequence = textView.text
    override fun setValue(thisRef: Any, property: KProperty<*>, value: CharSequence) {
        textView.text = value
    }
}