package com.dashlane.cryptography

class CryptographyFixedSalt(
    val data: ByteArray,
) {
    fun requireSize(size: Int): ByteArray {
        if (data.size != size) throw CryptographyInvalidSaltLengthException("Expected salt size $size, but was ${data.size}.")
        return data
    }

    override fun equals(other: Any?): Boolean {
        if (other !is CryptographyFixedSalt) return false
        if (!data.contentEquals(other.data)) return false
        return true
    }

    override fun hashCode(): Int =
        data.contentHashCode()
}

internal fun CryptographyFixedSalt?.requireNotNull() =
    this ?: throw CryptographyFixedSaltRequiredException("fixedSalt == null")