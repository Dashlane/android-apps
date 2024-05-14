package com.dashlane.nitro.cryptography

internal data class SecretStreamServerInfo(
    val header: ByteArray,
    val publicKey: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SecretStreamServerInfo

        if (!header.contentEquals(other.header)) return false
        return publicKey.contentEquals(other.publicKey)
    }

    override fun hashCode(): Int {
        var result = header.contentHashCode()
        result = 31 * result + publicKey.contentHashCode()
        return result
    }
}