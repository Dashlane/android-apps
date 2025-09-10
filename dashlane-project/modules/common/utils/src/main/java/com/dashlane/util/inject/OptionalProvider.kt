package com.dashlane.util.inject

fun interface OptionalProvider<T> {
    fun get(): T?
}