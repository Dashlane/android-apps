@file:JvmName("CursorUtils")

package com.dashlane.util

import android.database.Cursor

fun Cursor.getString(columnName: String): String? {
    val columnIndex = getColumnIndex(columnName)
    return if (columnIndex < 0) {
        null
    } else {
        getString(columnIndex)
    }
}

fun Cursor.getInt(columnName: String): Int {
    val columnIndex = getColumnIndex(columnName)
    return if (columnIndex < 0) {
        0
    } else {
        this.getInt(columnIndex)
    }
}

fun Cursor.getBoolean(columnName: String): Boolean {
    return getInt(columnName) == 1
}

fun Cursor.getLong(columnName: String): Long {
    val columnIndex = getColumnIndex(columnName)
    return if (columnIndex < 0) {
        0
    } else {
        getLong(columnIndex)
    }
}

fun Cursor?.closeCursor() {
    if (this != null && !isClosed) {
        close()
    }
}

fun Cursor?.getCountAndClose(): Int {
    if (this == null) {
        return 0
    }
    val count = count
    close()
    return count
}

@JvmOverloads
fun Cursor?.getStringListFromCursor(columnIndex: Int = 0): List<String> {
    if (this != null && columnIndex >= 0) {
        return toList { getString(columnIndex) }
    }
    return emptyList()
}

inline fun <T> Cursor.toList(transform: Cursor.() -> T): List<T> {
    val list = mutableListOf<T>()
    moveToFirst()
    while (!isAfterLast) {
        list.add(transform())
        moveToNext()
    }
    return list
}