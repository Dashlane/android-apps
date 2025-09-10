package com.dashlane.cryptography

import kotlin.jvm.JvmInline

@JvmInline
value class EncryptedByteArray(val value: ByteArray)

@Suppress("NOTHING_TO_INLINE")
inline fun ByteArray.asEncryptedByteArray() = EncryptedByteArray(this)