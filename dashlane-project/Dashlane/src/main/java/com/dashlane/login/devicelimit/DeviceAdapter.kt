package com.dashlane.login.devicelimit

import androidx.core.view.isVisible
import com.dashlane.login.Device
import com.dashlane.ui.adapter.DashlaneRecyclerAdapter
import com.skocken.efficientadapter.lib.viewholder.EfficientViewHolder

class DeviceAdapter(private val selectable: Boolean) : DashlaneRecyclerAdapter<Device>() {

    interface OnItemCheckListener {
        fun onItemCheckChanged(item: Device)
    }

    var itemCheckListener: OnItemCheckListener? = null

    override fun onBindViewHolder(viewHolder: EfficientViewHolder<Device>, position: Int) {
        val holder = viewHolder as DeviceViewHolder
        holder.checkBox.isVisible = selectable
        holder.itemCheckListener = itemCheckListener
        super.onBindViewHolder(viewHolder, position)
    }
}