package com.dashlane.util

internal inline fun <T, K> List<T>.updateCollection(
    updates: Collection<T>,
    deletes: Set<K>,
    keySelector: (T) -> K,
): List<T> {
    val updatesByKey = updates.associateBy(keySelector)
    return this.filter {
        keySelector(it).let { key ->
            !updatesByKey.containsKey(key) &&
                !deletes.contains(key)
        }
    } + updatesByKey.values
}