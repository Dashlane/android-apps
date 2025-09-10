package com.dashlane.ui.legacy.recyclerview.util

interface DiffUtilComparator<T> {
    fun isItemTheSame(item: T): Boolean

    fun isContentTheSame(item: T): Boolean
}