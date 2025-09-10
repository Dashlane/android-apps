package com.dashlane.cryptography

@JvmInline
value class SharingEncryptedByteArray(val value: ByteArray)

@Suppress("NOTHING_TO_INLINE")
inline fun ByteArray.asSharingEncryptedByteArray() = SharingEncryptedByteArray(this)