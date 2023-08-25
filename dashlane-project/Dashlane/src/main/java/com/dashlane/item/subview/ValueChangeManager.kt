package com.dashlane.item.subview

interface ValueChangeManager<T> {

    fun addValueChangedListener(listener: Listener<T>)
    fun removeValueChangedListener(listener: Listener<T>)
    fun notifyListeners(origin: Any, newValue: T)

    interface Listener<T> {
        fun onValueChanged(origin: Any, newValue: T)
    }
}