package com.dashlane.util

import android.view.View
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class ViewVisibilityDelegate(val view: View, val invisible: Int) : ReadWriteProperty<Any, Boolean> {

    override fun getValue(thisRef: Any, property: KProperty<*>): Boolean = view.visibility == View.VISIBLE

    override fun setValue(thisRef: Any, property: KProperty<*>, value: Boolean) {
        view.visibility = if (value) View.VISIBLE else invisible
    }
}