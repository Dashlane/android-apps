package com.dashlane.cryptography

fun interface MacFactory {
    fun createHmacSha256(key: ByteArray): Mac
}

fun MacFactory.createHmacSha256(key: ByteArray, iv: ByteArray) =
    createHmacSha256(key).apply { update(iv) }