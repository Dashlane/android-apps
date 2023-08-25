package com.dashlane.item.subview

import com.dashlane.R

abstract class ItemSubViewImpl<T> : ItemSubView<T> {

    override var topMargin: Int = R.dimen.spacing_normal

    override fun notifyValueChanged(newValue: T) {
        val previousValue = value
        if (previousValue != newValue) {
            value = newValue
            onItemValueHasChanged(previousValue, newValue)
        }
    }

    open fun onItemValueHasChanged(previousValue: T, newValue: T) {
        
    }
}