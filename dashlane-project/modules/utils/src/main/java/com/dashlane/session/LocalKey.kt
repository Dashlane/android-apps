package com.dashlane.session

import com.dashlane.cryptography.CryptographyKey
import com.dashlane.cryptography.ObfuscatedByteArray
import com.dashlane.cryptography.use
import okio.ByteString
import java.io.Closeable



class LocalKey : Closeable, Cloneable {

    private val _cryptographyKey: CryptographyKey.Raw32
    val cryptographyKey: CryptographyKey.Raw32
        get() = _cryptographyKey.clone()
    val cryptographyKeyBytes: ObfuscatedByteArray
        get() = _cryptographyKey.toObfuscatedByteArray()

    constructor(cryptographyKey: CryptographyKey.Raw32) {
        this._cryptographyKey = cryptographyKey.clone()
    }

    constructor(bytes: ByteArray) {
        this._cryptographyKey = CryptographyKey.ofBytes32(bytes)
    }

    public override fun clone() =
        LocalKey(_cryptographyKey)

    override fun close() {
        _cryptographyKey.close()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is LocalKey) return false
        if (_cryptographyKey != other._cryptographyKey) return false
        return true
    }

    override fun hashCode(): Int =
        _cryptographyKey.hashCode()
}



fun LocalKey.hex(): String =
    cryptographyKeyBytes.use(ObfuscatedByteArray::toByteArray).use(ByteString::of).hex()