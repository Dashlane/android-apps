package com.dashlane.cryptography

import kotlin.jvm.JvmInline

@JvmInline
value class EncryptedBase64String(val value: String)

@Suppress("NOTHING_TO_INLINE")
inline fun String.asEncryptedBase64() = EncryptedBase64String(this)