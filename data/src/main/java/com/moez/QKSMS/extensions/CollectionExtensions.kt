package com.moez.QKSMS.extensions

inline fun <K, T> Iterable<T>.associateByNotNull(keySelector: (T) -> K?): Map<K, T> {
    val map = hashMapOf<K, T>()
    forEach { value ->
        val key = keySelector(value)
        if (key != null) {
            map[key] = value
        }
    }

    return map
}
