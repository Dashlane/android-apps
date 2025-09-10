@file:Suppress("ktlint:standard:max-line-length")

package com.dashlane.xml.serializer.utils

internal inline fun <T, K, V> Iterable<T>.associateByNotNullKeys(keySelector: (T) -> K?, valueTransform: (T) -> V): Map<K, V> {
    return associateByNotNullKeysTo(mutableMapOf(), keySelector, valueTransform)
}

internal inline fun <T, K, V, M : MutableMap<in K, in V>> Iterable<T>.associateByNotNullKeysTo(destination: M, keySelector: (T) -> K?, valueTransform: (T) -> V): M {
    forEach { element -> keySelector(element)?.let { destination[it] = valueTransform(element) } }
    return destination
}