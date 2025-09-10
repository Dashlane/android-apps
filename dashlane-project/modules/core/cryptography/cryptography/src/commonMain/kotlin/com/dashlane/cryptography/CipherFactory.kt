package com.dashlane.cryptography

fun interface CipherFactory {
    fun createAesCbcCipher(
        mode: Mode,
        key: ByteArray,
        iv: ByteArray,
    ): Cipher

    enum class Mode {
        ENCRYPT,
        DECRYPT,
    }
}