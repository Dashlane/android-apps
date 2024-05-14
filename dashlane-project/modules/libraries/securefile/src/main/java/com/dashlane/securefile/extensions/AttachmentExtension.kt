package com.dashlane.securefile.extensions

import com.dashlane.cryptography.decodeBase64ToByteArray
import com.dashlane.securefile.Attachment
import com.dashlane.securefile.SecureFile

fun Attachment.toSecureFile(): SecureFile {
    return SecureFile(
        downloadKey,
        filename!!,
        cryptoKey!!.decodeBase64ToByteArray(),
        null
    )
}