package com.dashlane.ui.adapter.util

interface DiffUtilComparator<T> {
    fun isItemTheSame(item: T): Boolean
    fun isContentTheSame(item: T): Boolean
}