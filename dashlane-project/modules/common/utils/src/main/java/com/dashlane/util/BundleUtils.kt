package com.dashlane.util

import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import java.io.Serializable

inline fun <reified T : Parcelable> Bundle.getParcelableCompat(name: String): T? =
    getParcelableCompat(name, T::class.java)

@Suppress("DEPRECATION")
fun <T : Parcelable> Bundle.getParcelableCompat(name: String, clazz: Class<T>): T? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getParcelable(name, clazz)
    } else {
        getParcelable(name)
    }
}

inline fun <reified T : Parcelable> Bundle.getParcelableArrayListCompat(name: String): ArrayList<T>? =
    getParcelableArrayListCompat(name, T::class.java)

@Suppress("DEPRECATION")
fun <T : Parcelable> Bundle.getParcelableArrayListCompat(name: String, clazz: Class<T>): ArrayList<T>? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getParcelableArrayList(name, clazz)
    } else {
        getParcelableArrayList(name)
    }
}

inline fun <reified T : Parcelable?> Bundle.getParcelableArrayCompat(name: String): List<T>? =
    getParcelableArrayCompat(name, T::class.java)

@Suppress("DEPRECATION")
fun <T : Parcelable?> Bundle.getParcelableArrayCompat(name: String, clazz: Class<T>): List<T>? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getParcelableArray(name, clazz)?.toList()
    } else {
        getParcelableArray(name)?.map { it as T }
    }
}

inline fun <reified T : Serializable?> Bundle.getSerializableCompat(name: String): T? =
    getSerializableCompat(name, T::class.java)

@Suppress("DEPRECATION")
fun <T : Serializable?> Bundle.getSerializableCompat(key: String, clazz: Class<T>): T? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getSerializable(key, clazz)
    } else {
        getSerializable(key) as T
    }
}