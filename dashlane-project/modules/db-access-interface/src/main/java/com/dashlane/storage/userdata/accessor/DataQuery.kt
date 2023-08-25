package com.dashlane.storage.userdata.accessor

import com.dashlane.storage.userdata.accessor.filter.BaseFilter
import com.dashlane.vault.summary.SummaryObject

interface DataQuery<T : SummaryObject, F : BaseFilter> {

    fun createFilter(): F

    fun queryFirst(filter: F = createFilter()): T?

    fun queryAll(filter: F = createFilter()): List<T>

    fun count(filter: F = createFilter()): Int
}


inline fun <T : SummaryObject, F : BaseFilter> DataQuery<T, F>.queryFirst(filterConfiguration: F.() -> Unit = {}) =
    queryFirst(createFilter().apply(filterConfiguration))

inline fun <T : SummaryObject, F : BaseFilter> DataQuery<T, F>.queryAll(filterConfiguration: F.() -> Unit = {}) =
    queryAll(createFilter().apply(filterConfiguration))