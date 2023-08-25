package com.dashlane.attachment.ui

import com.dashlane.R
import com.dashlane.securefile.Attachment
import com.dashlane.ui.adapter.DashlaneRecyclerAdapter

class AttachmentItem : Attachment(), DashlaneRecyclerAdapter.ViewTypeProvider {
    enum class DownloadState {
        NOT_DOWNLOADED,
        DOWNLOADING,
        DOWNLOADED
    }

    @Transient
    var downloadState = DownloadState.NOT_DOWNLOADED

    @Transient
    var downloadProgress = 0

    @Transient
    var selected = false

    override fun getViewType(): DashlaneRecyclerAdapter.ViewType<out AttachmentItem> {
        return DashlaneRecyclerAdapter.ViewType<AttachmentItem>(
            R.layout.attachment_list_item,
            AttachmentViewHolder::class.java
        )
    }
}