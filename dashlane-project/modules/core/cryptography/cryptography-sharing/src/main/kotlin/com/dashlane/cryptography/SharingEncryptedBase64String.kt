package com.dashlane.cryptography

@JvmInline
value class SharingEncryptedBase64String(val value: String)

@Suppress("NOTHING_TO_INLINE")
inline fun String.asSharingEncryptedBase64() = SharingEncryptedBase64String(this)