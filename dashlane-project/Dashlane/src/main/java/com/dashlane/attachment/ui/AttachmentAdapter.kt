package com.dashlane.attachment.ui

import com.dashlane.ui.adapter.DashlaneRecyclerAdapter

class AttachmentAdapter : DashlaneRecyclerAdapter<AttachmentItem>() {

    interface OnAttachmentListChangesListener {
        fun onItemAdded(item: AttachmentItem)
        fun onItemRemoved(item: AttachmentItem)
    }

    var onAttachmentListChangesListener: OnAttachmentListChangesListener? = null

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
