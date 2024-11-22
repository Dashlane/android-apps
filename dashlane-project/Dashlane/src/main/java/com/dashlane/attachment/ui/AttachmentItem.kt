package com.dashlane.attachment.ui

import com.dashlane.R
import com.dashlane.securefile.Attachment
import com.dashlane.ui.adapter.DashlaneRecyclerAdapter

class AttachmentItem(
    val attachment: Attachment,
) : DashlaneRecyclerAdapter.ViewTypeProvider {
    enum class DownloadState {
        NOT_DOWNLOADED,
        DOWNLOADING,
        DOWNLOADED
    }

    var downloadState = DownloadState.NOT_DOWNLOADED
    var downloadProgress = 0
    var selected = false

    override fun getViewType(): DashlaneRecyclerAdapter.ViewType<out AttachmentItem> {
        return DashlaneRecyclerAdapter.ViewType(
            R.layout.attachment_list_item,
            AttachmentViewHolder::class.java
        )
    }
}