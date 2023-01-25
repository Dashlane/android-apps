package com.dashlane.securefile

import com.dashlane.cryptography.EncryptedFile



data class SecureFile(
    var id: String?,
    val fileName: String,
    val key: ByteArray,
    val encryptedFile: EncryptedFile?
)
