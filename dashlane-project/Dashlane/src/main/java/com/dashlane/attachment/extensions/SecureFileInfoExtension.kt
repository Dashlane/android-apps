package com.dashlane.attachment.extensions

import com.dashlane.attachment.ui.AttachmentItem
import com.dashlane.xml.domain.SyncObject

fun SyncObject.SecureFileInfo.toAttachmentItem(): AttachmentItem {
    return AttachmentItem().apply {
        id = this@toAttachmentItem.id
        type = this@toAttachmentItem.type
        filename = this@toAttachmentItem.filename
        downloadKey = this@toAttachmentItem.downloadKey
        cryptoKey = this@toAttachmentItem.cryptoKey
        localSize = this@toAttachmentItem.localSize!!.toLong()
        remoteSize = this@toAttachmentItem.remoteSize!!.toLong()
        
        creationDatetime = this@toAttachmentItem.creationDatetime!!.epochSecond
        userModificationDatetime = this@toAttachmentItem.userModificationDatetime!!.epochSecond
        owner = this@toAttachmentItem.owner
        version = this@toAttachmentItem.version!!.toInt()
    }
}