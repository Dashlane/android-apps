package com.dashlane.nitro.cryptography

internal data class SecretStreamStates(
    val encryptionState: ByteArray,
    val decryptionState: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SecretStreamStates

        if (!encryptionState.contentEquals(other.encryptionState)) return false
        if (!decryptionState.contentEquals(other.decryptionState)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = encryptionState.contentHashCode()
        result = 31 * result + decryptionState.contentHashCode()
        return result
    }
}