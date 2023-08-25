package com.dashlane.session

import com.dashlane.cryptography.CryptographyKey
import com.dashlane.cryptography.ObfuscatedByteArray
import com.dashlane.session.VaultKey.RemoteKey
import okio.Closeable

sealed class VaultKey : Closeable, Cloneable {

    abstract val cryptographyKey: CryptographyKey
    abstract val cryptographyKeyBytes: ObfuscatedByteArray

    public abstract override fun clone(): VaultKey

    class RemoteKey : VaultKey {

        private val _cryptographyKey: CryptographyKey.Raw64
        override val cryptographyKey: CryptographyKey.Raw64
            get() = _cryptographyKey.clone()
        override val cryptographyKeyBytes: ObfuscatedByteArray
            get() = _cryptographyKey.toObfuscatedByteArray()

        constructor(cryptographyKey: CryptographyKey.Raw64) {
            this._cryptographyKey = cryptographyKey.clone()
        }

        constructor(bytes: ObfuscatedByteArray) {
            this._cryptographyKey = CryptographyKey.ofBytes64(bytes)
        }

        constructor(bytes: ByteArray) {
            this._cryptographyKey = CryptographyKey.ofBytes64(bytes)
        }

        override fun clone() =
            RemoteKey(_cryptographyKey)

        override fun close() {
            _cryptographyKey.close()
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is RemoteKey) return false
            if (_cryptographyKey != other._cryptographyKey) return false
            return true
        }

        override fun hashCode(): Int =
            _cryptographyKey.hashCode()
    }

    class Password : VaultKey {

        private val _appKey: AppKey.Password
        val appKey: AppKey.Password
            get() = _appKey.clone()
        val passwordUtf8Bytes: ObfuscatedByteArray
            get() = appKey.passwordUtf8Bytes
        val serverKeyUtf8Bytes: ObfuscatedByteArray?
            get() = appKey.serverKeyUtf8Bytes
        override val cryptographyKey: CryptographyKey.Password
            get() = _appKey.cryptographyKey
        override val cryptographyKeyBytes: ObfuscatedByteArray
            get() = _appKey.cryptographyKeyBytes
        val isServerKeyNotNull
            get() = _appKey.isServerKeyNotNull

        constructor(appKey: AppKey.Password) {
            this._appKey = appKey.clone()
        }

        constructor(passwordUtf8Bytes: ObfuscatedByteArray, serverKeyUtf8Bytes: ObfuscatedByteArray? = null) {
            this._appKey = AppKey.Password(passwordUtf8Bytes, serverKeyUtf8Bytes)
        }

        constructor(password: CharSequence, serverKey: CharSequence? = null) {
            this._appKey = AppKey.Password(password, serverKey)
        }

        override fun clone() =
            Password(_appKey)

        override fun close() {
            _appKey.close()
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Password) return false
            if (_appKey != other._appKey) return false
            return true
        }

        override fun hashCode(): Int =
            _appKey.hashCode()
    }
}

val VaultKey.isServerKeyNotNull: Boolean
    get() = this is VaultKey.Password && isServerKeyNotNull

val VaultKey.serverKeyUtf8Bytes: ObfuscatedByteArray?
    get() = (this as? VaultKey.Password)?.serverKeyUtf8Bytes