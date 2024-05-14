package com.dashlane.session

import com.dashlane.cryptography.CryptographyKey
import com.dashlane.cryptography.ObfuscatedByteArray
import com.dashlane.cryptography.clone
import com.dashlane.cryptography.encodeUtf8ToObfuscated
import com.dashlane.cryptography.use
import okio.Closeable
import okio.use
import kotlin.experimental.xor

sealed class AppKey : Closeable, Cloneable {

    abstract val cryptographyKey: CryptographyKey
    abstract val cryptographyKeyType: CryptographyKey.Type
    abstract val cryptographyKeyBytes: ObfuscatedByteArray

    public abstract override fun clone(): AppKey

    class SsoKey : AppKey {
        override val cryptographyKeyType: CryptographyKey.Type
            get() = CryptographyKey.Type.RAW_64
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
            SsoKey(_cryptographyKey)

        override fun close() {
            _cryptographyKey.close()
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is SsoKey) return false
            if (_cryptographyKey != other._cryptographyKey) return false
            return true
        }

        override fun hashCode(): Int =
            _cryptographyKey.hashCode()

        companion object {

            fun create(
                serverKey: ByteArray,
                serviceProviderKey: ByteArray
            ): SsoKey {
                require(serverKey.size == CryptographyKey.KEY_SIZE_64) { "Unexpected server key size ${serverKey.size}" }
                require(serviceProviderKey.size == CryptographyKey.KEY_SIZE_64) { "Unexpected SP key size ${serverKey.size}" }
                return ByteArray(CryptographyKey.KEY_SIZE_64) { i -> serverKey[i] xor serviceProviderKey[i] }.use(AppKey::SsoKey)
            }
        }
    }

    class Password : AppKey {

        override val cryptographyKeyType: CryptographyKey.Type
            get() = CryptographyKey.Type.PASSWORD
        private val _passwordUtf8Bytes: ObfuscatedByteArray
        val passwordUtf8Bytes: ObfuscatedByteArray
            get() = _passwordUtf8Bytes.clone()
        private val _serverKeyUtf8Bytes: ObfuscatedByteArray?
        val serverKeyUtf8Bytes: ObfuscatedByteArray?
            get() = _serverKeyUtf8Bytes?.clone()

        override val cryptographyKey: CryptographyKey.Password
            get() =
                if (_serverKeyUtf8Bytes == null) {
                    CryptographyKey.ofPasswordUtf8Bytes(_passwordUtf8Bytes)
                } else {
                    (_serverKeyUtf8Bytes + _passwordUtf8Bytes).use(CryptographyKey::ofPasswordUtf8Bytes)
                }
        override val cryptographyKeyBytes: ObfuscatedByteArray
            get() =
                if (_serverKeyUtf8Bytes == null) {
                    _passwordUtf8Bytes.clone()
                } else {
                    (_serverKeyUtf8Bytes + _passwordUtf8Bytes)
                }

        val isServerKeyNotNull
            get() = _serverKeyUtf8Bytes != null

        constructor(passwordUtf8Bytes: ObfuscatedByteArray, serverKeyUtf8Bytes: ObfuscatedByteArray? = null) {
            this._passwordUtf8Bytes = passwordUtf8Bytes.clone()
            this._serverKeyUtf8Bytes = serverKeyUtf8Bytes?.clone()
        }

        constructor(password: CharSequence, serverKey: CharSequence? = null) {
            this._passwordUtf8Bytes = password.encodeUtf8ToObfuscated()
            this._serverKeyUtf8Bytes = serverKey?.encodeUtf8ToObfuscated()
        }

        fun toVaultKey() =
            VaultKey.Password(this)

        override fun clone() =
            Password(_passwordUtf8Bytes, _serverKeyUtf8Bytes)

        override fun close() {
            _passwordUtf8Bytes.close()
            _serverKeyUtf8Bytes?.close()
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Password) return false
            if (_passwordUtf8Bytes != other._passwordUtf8Bytes) return false
            if (_serverKeyUtf8Bytes != other._serverKeyUtf8Bytes) return false
            return true
        }

        override fun hashCode(): Int {
            var result = _passwordUtf8Bytes.hashCode()
            result = 31 * result + (_serverKeyUtf8Bytes?.hashCode() ?: 0)
            return result
        }
    }
}

val AppKey.userKeyBytes: ObfuscatedByteArray
    get() = if (this is AppKey.Password) passwordUtf8Bytes else cryptographyKeyBytes

val AppKey.isServerKeyNotNull: Boolean
    get() = this is AppKey.Password && isServerKeyNotNull

val AppKey.serverKeyUtf8Bytes: ObfuscatedByteArray?
    get() = (this as? AppKey.Password)?.serverKeyUtf8Bytes

fun AppKey.Password.decodeServerKeyUtf8ToString(): String? =
    serverKeyUtf8Bytes?.use(ObfuscatedByteArray::decodeUtf8ToString)

fun AppKey.Password.decodeCryptographyKeyUtf8ToString(): String =
    cryptographyKeyBytes.use(ObfuscatedByteArray::decodeUtf8ToString)