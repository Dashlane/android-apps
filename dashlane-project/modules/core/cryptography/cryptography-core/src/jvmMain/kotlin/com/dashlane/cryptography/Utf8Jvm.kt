package com.dashlane.cryptography

import java.nio.ByteBuffer
import java.nio.CharBuffer

actual fun CharArray.encodeUtf8ToByteArray(): ByteArray =
    CharBuffer.wrap(this).encodeUtf8ToByteArray()

actual fun CharSequence.encodeUtf8ToByteArray(): ByteArray =
    CharBuffer.wrap(this).encodeUtf8ToByteArray()

private fun CharBuffer.encodeUtf8ToByteArray(): ByteArray =
    Charsets.UTF_8.encode(this).use(ByteBuffer::toByteArray)

private fun ByteBuffer.toByteArray(): ByteArray =
    array().copyOfRange(position(), limit())

private inline fun <T> ByteBuffer.use(block: (ByteBuffer) -> T): T =
    try {
        block(this)
    } finally {
        array().close()
    }

actual fun ByteArray.decodeUtf8ToCharArray(): CharArray =
    decodeUtf8ToCharBuffer().use(CharBuffer::toCharArray)

actual fun ByteArray.decodeUtf8ToString(): String =
    decodeUtf8ToCharBuffer().use(CharBuffer::toString)

private fun ByteArray.decodeUtf8ToCharBuffer() =
    Charsets.UTF_8.decode(ByteBuffer.wrap(this))

private fun CharBuffer.toCharArray(): CharArray =
    array().copyOfRange(position(), limit())

private inline fun <T> CharBuffer.use(block: (CharBuffer) -> T): T =
    try {
        block(this)
    } finally {
        array().close()
    }