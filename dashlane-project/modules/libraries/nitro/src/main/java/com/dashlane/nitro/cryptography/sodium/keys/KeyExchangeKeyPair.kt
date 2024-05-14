package com.dashlane.nitro.cryptography.sodium.keys

data class KeyExchangeKeyPair(
    val publicKey: ByteArray,
    val secretKey: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as KeyExchangeKeyPair

        if (!publicKey.contentEquals(other.publicKey)) return false
        return secretKey.contentEquals(other.secretKey)
    }

    override fun hashCode(): Int {
        var result = publicKey.contentHashCode()
        result = 31 * result + secretKey.contentHashCode()
        return result
    }
}