package com.dashlane.util

import androidx.appcompat.widget.SwitchCompat
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class CheckedDelegate(private val switchCompat: SwitchCompat) : ReadWriteProperty<Any, Boolean> {
    override fun getValue(thisRef: Any, property: KProperty<*>): Boolean =
        switchCompat.isChecked

    override fun setValue(thisRef: Any, property: KProperty<*>, value: Boolean) {
        switchCompat.isChecked = value
    }
}