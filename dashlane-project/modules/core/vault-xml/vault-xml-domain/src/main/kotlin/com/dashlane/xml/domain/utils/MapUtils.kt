package com.dashlane.xml.domain.utils

internal operator fun <K, V : Any> MutableMap<K, V>.set(key: K, value: V?) =
    if (value == null) {
        remove(key)
    } else {
        put(key, value)
    }

internal inline fun <K, V, R> Map<K, V>.mapValuesNotNull(transform: (Map.Entry<K, V>) -> R?): Map<K, R> {
    val result = mutableMapOf<K, R>()
    for (entry in this) {
        val value = transform(entry)
        if (value != null) {
            result[entry.key] = value
        }
    }
    return result
}