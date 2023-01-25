package com.dashlane.attachment.ui

import com.dashlane.ui.adapter.DashlaneRecyclerAdapter
import com.skocken.efficientadapter.lib.viewholder.EfficientViewHolder

class AttachmentAdapter : DashlaneRecyclerAdapter<AttachmentItem>() {

    interface OnAttachmentListChangesListener {
        fun onItemAdded(item: AttachmentItem)
        fun onItemRemoved(item: AttachmentItem)
    }

    var onIconClickListener: AttachmentViewHolder.OnIconClickListener? = null
    var onAttachmentListChangesListener: OnAttachmentListChangesListener? = null

    override fun onBindViewHolder(viewHolder: EfficientViewHolder<AttachmentItem>, position: Int) {
        val holder = viewHolder as AttachmentViewHolder
        holder.iconClickListener = onIconClickListener
        super.onBindViewHolder(viewHolder, position)
    }

    override fun add(item: AttachmentItem?) {
        super.add(item)
        onAttachmentListChangesListener?.onItemAdded(item!!)
    }

    override fun removeAt(position: Int) {
        val item = get(position)
        super.removeAt(position)
        onAttachmentListChangesListener?.onItemRemoved(item)
    }
}
