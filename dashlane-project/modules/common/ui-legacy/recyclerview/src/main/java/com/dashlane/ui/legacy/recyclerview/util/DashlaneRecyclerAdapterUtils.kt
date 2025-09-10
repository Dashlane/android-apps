package com.dashlane.ui.legacy.recyclerview.util

import androidx.recyclerview.widget.DiffUtil
import com.dashlane.ui.legacy.recyclerview.DashlaneRecyclerAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun <T : DashlaneRecyclerAdapter.ViewTypeProvider> DashlaneRecyclerAdapter<T>.populateItemsAsync(list: List<T>) {
    val diffResult = withContext(Dispatchers.Default) {
        DiffUtil.calculateDiff(AdapterViewTypeProviderDiffCallback(objects, list))
    }
    setNotifyOnChange(false)
    clear()
    addAll(list)
    setNotifyOnChange(true)
    diffResult.dispatchUpdatesTo(this)
}