package com.dashlane.util

import android.content.Intent
import android.os.Build
import android.os.Parcelable
import java.io.Serializable

fun Intent.clearTop(): Intent = apply { addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP) }

fun Intent.singleTop(): Intent = apply { addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP) }

fun Intent.newTask(): Intent = apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }

fun Intent.clearTask(): Intent = apply { addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK) }

inline fun <reified T : Parcelable> Intent.getParcelableExtraCompat(name: String): T? =
    getParcelableExtraCompat(name, T::class.java)

@Suppress("DEPRECATION")
fun <T : Parcelable> Intent.getParcelableExtraCompat(name: String, clazz: Class<T>): T? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getParcelableExtra(name, clazz)
    } else {
        getParcelableExtra(name)
    }
}

inline fun <reified T : Parcelable> Intent.getParcelableArrayListCompat(name: String): List<T>? =
    getParcelableArrayListCompat(name, T::class.java)

@Suppress("DEPRECATION")
fun <T : Parcelable> Intent.getParcelableArrayListCompat(name: String, clazz: Class<T>): List<T>? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getParcelableArrayListExtra(name, clazz)
    } else {
        getParcelableArrayListExtra(name)
    }
}

inline fun <reified T : Parcelable?> Intent.getParcelableArrayCompat(name: String): List<T>? =
    getParcelableArrayCompat(name, T::class.java)

@Suppress("DEPRECATION")
fun <T : Parcelable?> Intent.getParcelableArrayCompat(name: String, clazz: Class<T>): List<T>? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getParcelableArrayExtra(name, clazz)?.toList()
    } else {
        getParcelableArrayExtra(name)?.map { it as T }
    }
}

inline fun <reified T : Serializable?> Intent.getSerializableExtraCompat(name: String): T? =
    getSerializableExtraCompat(name, T::class.java)

@Suppress("DEPRECATION")
fun <T : Serializable?> Intent.getSerializableExtraCompat(name: String, clazz: Class<T>): T? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getSerializableExtra(name, clazz)
    } else {
        getSerializableExtra(name) as T
    }
}
