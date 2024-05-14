package com.dashlane.nitro.cryptography

internal data class SecretStreamClientInfo(
    val header: ByteArray,
    val states: SecretStreamStates
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SecretStreamClientInfo

        if (!header.contentEquals(other.header)) return false
        if (states != other.states) return false

        return true
    }

    override fun hashCode(): Int {
        var result = header.contentHashCode()
        result = 31 * result + states.hashCode()
        return result
    }
}