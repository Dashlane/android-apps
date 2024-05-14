package com.dashlane.util

import android.view.View
import com.dashlane.design.component.compat.view.BadgeView
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class BadgeViewOptionalText(
    val badgeView: BadgeView,
    private val nullVisibility: Int
) : ReadWriteProperty<Any, String> {
    override fun getValue(thisRef: Any, property: KProperty<*>): String = badgeView.text
    override fun setValue(thisRef: Any, property: KProperty<*>, value: String) {
        badgeView.text = value
        badgeView.visibility = if (value.isBlank()) nullVisibility else View.VISIBLE
    }
}