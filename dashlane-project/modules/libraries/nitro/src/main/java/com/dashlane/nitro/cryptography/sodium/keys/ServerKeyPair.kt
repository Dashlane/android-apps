package com.dashlane.nitro.cryptography.sodium.keys

data class ServerKeyPair(
    val rx: ByteArray,
    val tx: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as KeyExchangeKeyPair

        if (!rx.contentEquals(other.publicKey)) return false
        return tx.contentEquals(other.secretKey)
    }

    override fun hashCode(): Int {
        var result = rx.contentHashCode()
        result = 31 * result + tx.contentHashCode()
        return result
    }
}
