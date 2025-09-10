package com.dashlane.cryptography

import okio.ByteString.Companion.decodeBase64
import okio.ByteString.Companion.toByteString

@Throws(CryptographyBase64Exception::class)
fun String.decodeBase64ToByteArray(): ByteArray =
    decodeBase64ToByteArrayOrNull() ?: throw CryptographyBase64Exception()

fun String.decodeBase64ToByteArrayOrNull(): ByteArray? =
    decodeBase64()?.toByteArray()

@Throws(CryptographyBase64Exception::class)
fun String.decodeBase64ToUtf8String(): String =
    decodeBase64ToUtf8StringOrNull() ?: throw CryptographyBase64Exception()

fun String.decodeBase64ToUtf8StringOrNull(): String? =
    decodeBase64()?.utf8()

fun ByteArray.encodeBase64ToString(): String =
    toByteString().base64()