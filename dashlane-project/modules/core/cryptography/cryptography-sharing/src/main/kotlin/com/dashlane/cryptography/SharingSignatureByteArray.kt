package com.dashlane.cryptography

@JvmInline
value class SharingSignatureByteArray(val value: ByteArray)

@Suppress("NOTHING_TO_INLINE")
inline fun ByteArray.asSharingSignatureByteArray() = SharingSignatureByteArray(this)