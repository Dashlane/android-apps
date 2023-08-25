package com.dashlane.item.subview

import androidx.annotation.DimenRes

interface ItemSubView<T> {

    var value: T

    @get:DimenRes
    var topMargin: Int

    fun notifyValueChanged(newValue: T)
}