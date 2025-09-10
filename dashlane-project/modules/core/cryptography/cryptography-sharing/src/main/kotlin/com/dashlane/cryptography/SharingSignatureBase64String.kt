package com.dashlane.cryptography

@JvmInline
value class SharingSignatureBase64String(val value: String)

@Suppress("NOTHING_TO_INLINE")
inline fun String.asSharingSignatureBase64() = SharingSignatureBase64String(this)