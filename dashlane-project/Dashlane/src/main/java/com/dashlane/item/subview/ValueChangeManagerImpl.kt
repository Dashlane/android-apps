package com.dashlane.item.subview

class ValueChangeManagerImpl<T> : ValueChangeManager<T> {

    private val listeners = mutableSetOf<ValueChangeManager.Listener<T>>()

    override fun addValueChangedListener(listener: ValueChangeManager.Listener<T>) {
        listeners.add(listener)
    }

    override fun removeValueChangedListener(listener: ValueChangeManager.Listener<T>) {
        listeners.remove(listener)
    }

    override fun notifyListeners(origin: Any, newValue: T) {
        listeners.forEach { it.onValueChanged(origin, newValue) }
    }
}