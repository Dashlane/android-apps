package com.dashlane.cryptography

import java.security.MessageDigest

private const val SHA_1 = "SHA-1"
private const val SHA_512 = "SHA-512"

internal actual fun deriveKwc3iv(cipherKey: ByteArray, salt: ByteArray): ByteArray =
    ByteArray(SHA_1_HASH_SIZE * 3).use { buffer ->
        deriveKwc3iv(cipherKey, salt, buffer)
    }

private fun deriveKwc3iv(
    cipherKey: ByteArray,
    salt: ByteArray,
    buffer: ByteArray,
): ByteArray {
    val digest = MessageDigest.getInstance(SHA_1)

    
    digest.update(cipherKey)
    digest.update(salt, 0, KWC3_SALT_IV_UPDATE_LENGTH)
    digest.digest(buffer, 0, SHA_1_HASH_SIZE)

    
    digest.update(buffer, 0, SHA_1_HASH_SIZE) 
    digest.update(cipherKey)
    digest.update(salt, 0, KWC3_SALT_IV_UPDATE_LENGTH)
    digest.digest(buffer, SHA_1_HASH_SIZE, SHA_1_HASH_SIZE)

    
    digest.update(buffer, SHA_1_HASH_SIZE, SHA_1_HASH_SIZE) 
    digest.update(cipherKey)
    digest.update(salt, 0, KWC3_SALT_IV_UPDATE_LENGTH)
    digest.digest(buffer, SHA_1_HASH_SIZE * 2, SHA_1_HASH_SIZE)

    
    return buffer.copyOfRange(32, 32 + AES_IV_SIZE)
}

internal actual fun sha512(value: ByteArray): ByteArray =
    MessageDigest.getInstance(SHA_512).digest(value)