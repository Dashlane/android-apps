package com.dashlane.nitro.cryptography.sodium

import com.dashlane.nitro.cryptography.sodium.SodiumCryptography.Companion.SODIUM_PUBLIC_KEY_BYTES
import com.dashlane.nitro.cryptography.sodium.SodiumCryptography.Companion.SODIUM_SECRET_KEY_BYTES
import com.dashlane.nitro.cryptography.sodium.SodiumCryptography.Companion.SODIUM_SESSION_KEY_BYTES
import com.dashlane.nitro.cryptography.sodium.keys.ClientKeyPair
import com.dashlane.nitro.cryptography.sodium.keys.KeyExchangeKeyPair
import com.dashlane.nitro.cryptography.sodium.keys.ServerKeyPair
import com.dashlane.sodium.jni.SodiumJni
import javax.inject.Inject

interface SodiumCryptography {

    fun generateKeyExchangeKeyPair(): KeyExchangeKeyPair?

    fun keyExchangeClientSessionKeys(
        clientKeyPair: KeyExchangeKeyPair,
        serverPublicKey: ByteArray
    ): ClientKeyPair?

    fun keyExchangeServerSessionKeys(
        serverKeyPair: KeyExchangeKeyPair,
        clientPublicKey: ByteArray
    ): ServerKeyPair?

    fun secretstreamXchacha20poly1305InitPush(
        state: ByteArray,
        header: ByteArray,
        key: ByteArray
    ): Boolean

    fun secretstreamXchacha20poly1305InitPull(
        state: ByteArray,
        header: ByteArray,
        key: ByteArray
    ): Boolean

    fun secretstreamXchacha20poly1305Push(
        state: ByteArray,
        message: ByteArray,
        encrypted: ByteArray
    ): Boolean

    fun secretstreamXchacha20poly1305Pull(
        state: ByteArray,
        message: ByteArray,
        encrypted: ByteArray
    ): Boolean

    fun secretboxEasy(
        message: ByteArray,
        nonce: ByteArray,
        key: ByteArray
    ): ByteArray?

    fun secretboxOpenEasy(
        cipherText: ByteArray,
        nonce: ByteArray,
        key: ByteArray
    ): ByteArray?

    fun randomBuf(
        size: Int,
    ): ByteArray

    fun genericHash(
        message: ByteArray,
        key: ByteArray
    ): ByteArray

    fun genericHash(
        message: ByteArray
    ): ByteArray

    fun sodiumMemcmp(
        key1: ByteArray,
        key2: ByteArray
    ): Boolean

    companion object {
        const val ENCRYPTION_KEY_HEADER = "DASHLANE_D2D_SYMMETRIC_KEY"
        const val WORDSEED_KEY_HEADER = "DASHLANE_D2D_SAS_SEED"

        const val SODIUM_PUBLIC_KEY_BYTES = 32
        const val SODIUM_SECRET_KEY_BYTES = 32
        const val SODIUM_SESSION_KEY_BYTES = 32
        const val SODIUM_STATE_BYTES = 52
        const val SODIUM_HEADER_BYTES = 24
        const val ENCRYPTED_EXTRA_BYTES = 17
        const val SODIUM_NONCE_BYTES = 24
    }
}

class SodiumCryptographyImpl @Inject constructor() : SodiumCryptography {
    override fun generateKeyExchangeKeyPair(): KeyExchangeKeyPair? {
        val publicKey = ByteArray(SODIUM_PUBLIC_KEY_BYTES)
        val secretKey = ByteArray(SODIUM_SECRET_KEY_BYTES)

        if (!SodiumJni.crypto_kx_keypair(publicKey, secretKey)) {
            return null
        }

        return KeyExchangeKeyPair(
            publicKey = publicKey,
            secretKey = secretKey
        )
    }

    override fun keyExchangeClientSessionKeys(
        clientKeyPair: KeyExchangeKeyPair,
        serverPublicKey: ByteArray
    ): ClientKeyPair? {
        val rx = ByteArray(SODIUM_SESSION_KEY_BYTES)
        val tx = ByteArray(SODIUM_SESSION_KEY_BYTES)

        if (!SodiumJni.crypto_kx_client_session_keys(
                rx = rx,
                tx = tx,
                publicKey = clientKeyPair.publicKey,
                secretKey = clientKeyPair.secretKey,
                peerPublicKey = serverPublicKey
            )
        ) {
            return null
        }
        return ClientKeyPair(rx = rx, tx = tx)
    }

    override fun keyExchangeServerSessionKeys(
        serverKeyPair: KeyExchangeKeyPair,
        clientPublicKey: ByteArray
    ): ServerKeyPair? {
        val rx = ByteArray(SODIUM_SESSION_KEY_BYTES)
        val tx = ByteArray(SODIUM_SESSION_KEY_BYTES)

        if (!SodiumJni.crypto_kx_server_session_keys(
                rx = rx,
                tx = tx,
                publicKey = serverKeyPair.publicKey,
                secretKey = serverKeyPair.secretKey,
                clientPublicKey = clientPublicKey
            )
        ) {
            return null
        }
        return ServerKeyPair(rx = rx, tx = tx)
    }

    override fun secretstreamXchacha20poly1305InitPush(
        state: ByteArray,
        header: ByteArray,
        key: ByteArray
    ) = SodiumJni.crypto_secretstream_xchacha20poly1305_init_push(
        state = state,
        header = header,
        key = key
    )

    override fun secretstreamXchacha20poly1305InitPull(
        state: ByteArray,
        header: ByteArray,
        key: ByteArray
    ) =
        SodiumJni.crypto_secretstream_xchacha20poly1305_init_pull(
            state = state,
            header = header,
            key = key
        )

    override fun secretstreamXchacha20poly1305Push(
        state: ByteArray,
        message: ByteArray,
        encrypted: ByteArray
    ): Boolean = SodiumJni.crypto_secretstream_xchacha20poly1305_push(
        state = state,
        message = message,
        encrypted = encrypted
    )

    override fun secretstreamXchacha20poly1305Pull(
        state: ByteArray,
        message: ByteArray,
        encrypted: ByteArray
    ): Boolean = SodiumJni.crypto_secretstream_xchacha20poly1305_pull(
        state = state,
        message = message,
        encrypted = encrypted
    )

    override fun secretboxEasy(message: ByteArray, nonce: ByteArray, key: ByteArray): ByteArray? {
        return SodiumJni.crypto_secretbox_easy(
            message = message,
            nonce = nonce,
            key = key
        )
    }

    override fun secretboxOpenEasy(cipherText: ByteArray, nonce: ByteArray, key: ByteArray): ByteArray? {
        return SodiumJni.crypto_secretbox_open_easy(
            cipherText = cipherText,
            nonce = nonce,
            key = key
        )
    }

    override fun randomBuf(size: Int): ByteArray = SodiumJni.randombytes_buf(size)

    override fun genericHash(
        message: ByteArray,
        key: ByteArray
    ): ByteArray {
        return SodiumJni.crypto_generichash(
            SODIUM_SECRET_KEY_BYTES,
            message,
            message.size.toLong(),
            key,
            key.size
        )
    }

    override fun genericHash(message: ByteArray): ByteArray = SodiumJni.crypto_generichash(SODIUM_SECRET_KEY_BYTES, message, message.size.toLong())

    override fun sodiumMemcmp(key1: ByteArray, key2: ByteArray): Boolean = SodiumJni.sodium_memcmp(key1, key2)
}