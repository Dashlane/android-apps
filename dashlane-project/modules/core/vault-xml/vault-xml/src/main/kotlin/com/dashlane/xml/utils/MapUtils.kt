package com.dashlane.xml.utils

internal inline fun <K, V, reified R : V> Map<K, V>.filterValuesInstanceOf(): Map<K, R> {
    val result = mutableMapOf<K, R>()
    for ((key, value) in this) {
        if (value is R) {
            result[key] = value
        }
    }
    return result
}