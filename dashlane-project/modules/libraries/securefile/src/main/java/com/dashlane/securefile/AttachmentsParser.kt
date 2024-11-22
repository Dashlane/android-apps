package com.dashlane.securefile

import com.dashlane.util.isSemanticallyNull
import com.google.gson.Gson

class AttachmentsParser {

    fun parse(jsonAttachments: String?): Array<Attachment> {
        if (jsonAttachments.isSemanticallyNull()) {
            return emptyArray()
        }
        return Gson().fromJson(jsonAttachments, Array<Attachment>::class.java)
            ?: emptyArray()
    }
}