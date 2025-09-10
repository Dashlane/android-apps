package com.dashlane.hermes

import okio.ByteString.Companion.encodeUtf8

class Sha256Hash private constructor(val hex: String) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Sha256Hash) return false
        if (hex != other.hex) return false
        return true
    }

    override fun hashCode(): Int {
        return hex.hashCode()
    }

    override fun toString(): String = "Sha256Hash(hex='$hex')"

    companion object {
        fun of(s: String) = Sha256Hash(s.encodeUtf8().sha256().hex())
    }
}