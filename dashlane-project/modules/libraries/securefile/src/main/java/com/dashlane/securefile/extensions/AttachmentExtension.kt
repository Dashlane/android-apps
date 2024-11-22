package com.dashlane.securefile.extensions

import com.dashlane.cryptography.decodeBase64ToByteArray
import com.dashlane.securefile.Attachment
import com.dashlane.securefile.AttachmentsParser
import com.dashlane.securefile.SecureFile
import com.dashlane.util.isNotSemanticallyNull
import com.dashlane.vault.summary.SummaryObject

fun Attachment.toSecureFile(): SecureFile {
    return SecureFile(
        downloadKey,
        filename!!,
        cryptoKey!!.decodeBase64ToByteArray(),
        null
    )
}

fun SummaryObject.hasAttachments(): Boolean =
    attachments.isNotSemanticallyNull() && !attachments.isNullOrBlank() && attachments != "[]"

fun SummaryObject.attachmentsCount(): Int = AttachmentsParser().parse(attachments).size
