package com.dashlane.cryptography

internal const val SHA_1_HASH_SIZE = 20
internal const val KWC3_SALT_IV_UPDATE_LENGTH = 8

internal const val HMAC_KEY_SIZE = 32
internal const val HMAC_VALUE_SIZE = 32
internal const val AES_KEY_SIZE = 32
internal const val AES_IV_SIZE = 16

internal const val LEGACY_MARKER_HEADER_DATA_LENGTH = 32

internal expect fun deriveKwc3iv(cipherKey: ByteArray, salt: ByteArray): ByteArray

internal fun expandCipherMacKeyPair(key: ByteArray): Pair<ByteArray, ByteArray> =
    sha512(key).use { it.copyOfRange(0, AES_KEY_SIZE) to it.copyOfRange(AES_KEY_SIZE, AES_KEY_SIZE + HMAC_KEY_SIZE) }

internal expect fun sha512(value: ByteArray): ByteArray