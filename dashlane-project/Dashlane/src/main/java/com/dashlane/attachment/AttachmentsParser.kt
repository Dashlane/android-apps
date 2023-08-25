@file:JvmName("AttachmentsParser")

package com.dashlane.attachment

import com.dashlane.attachment.ui.AttachmentItem
import com.dashlane.util.isSemanticallyNull
import com.google.gson.Gson

class AttachmentsParser {

    fun parse(jsonAttachments: String?): Array<AttachmentItem> {
        if (jsonAttachments.isSemanticallyNull()) {
            return emptyArray()
        }
        return Gson().fromJson<Array<AttachmentItem>>(jsonAttachments, Array<AttachmentItem>::class.java)
            ?: emptyArray()
    }
}