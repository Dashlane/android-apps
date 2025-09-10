package com.dashlane.cryptography

import java.io.File

@JvmInline
value class EncryptedFile(val value: File)

@Suppress("NOTHING_TO_INLINE")
inline fun File.asEncryptedFile() = EncryptedFile(this)