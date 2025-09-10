package com.dashlane.cryptography

import okio.Buffer
import okio.BufferedSource
import okio.ByteString
import okio.ByteString.Companion.decodeBase64

fun interface DecryptionSource {
    fun open(): BufferedSource
}

@Throws(CryptographyBase64Exception::class)
fun EncryptedBase64String.asBase64DecryptionSource(): DecryptionSource =
    (value.decodeBase64() ?: throw CryptographyBase64Exception("Invalid Base64")).asDecryptionSource()

private fun ByteString.asDecryptionSource(): DecryptionSource =
    DecryptionSource(::toBuffer)

private fun ByteString.toBuffer() =
    Buffer().write(this)

fun EncryptedByteArray.asDecryptionSource(): DecryptionSource =
    DecryptionSource { value.toBuffer() }

private fun ByteArray.toBuffer() =
    Buffer().write(this)