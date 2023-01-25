package com.dashlane.ui.adapter.util

import androidx.recyclerview.widget.DiffUtil
import com.dashlane.ui.adapter.DashlaneRecyclerAdapter
import com.dashlane.ui.adapter.DashlaneRecyclerAdapter.ViewTypeProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext



suspend fun <T : ViewTypeProvider> DashlaneRecyclerAdapter<T>.populateItemsAsync(list: List<T>) {
    val diffResult = withContext(Dispatchers.Default) {
        DiffUtil.calculateDiff(AdapterViewTypeProviderDiffCallback(objects, list))
    }
    setNotifyOnChange(false)
    clear()
    addAll(list)
    setNotifyOnChange(true)
    diffResult.dispatchUpdatesTo(this)
}