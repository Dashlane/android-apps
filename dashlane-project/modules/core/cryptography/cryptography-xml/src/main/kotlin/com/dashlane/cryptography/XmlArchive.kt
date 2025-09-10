package com.dashlane.cryptography

import com.dashlane.xml.XmlBackup
import com.dashlane.xml.serializer.XmlException

data class XmlArchive(
    val ids: List<String>?,
    val data: EncryptedBase64String?,
)

@Throws(CryptographyException::class, XmlException::class)
fun XmlDecryptionEngine.decryptSecureArchive(archive: XmlArchive): XmlBackup? =
    if (archive.data == null) {
        null
    } else {
        decryptBase64ToXmlBackup(archive.data)
    }

fun XmlEncryptionEngine.encryptSecureArchive(ids: List<String>, backup: XmlBackup) =
    XmlArchive(
        ids,
        encryptXmlBackupToBase64String(backup),
    )