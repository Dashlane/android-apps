package com.dashlane.nitro.cryptography

import com.dashlane.nitro.NitroCryptographyException
import com.dashlane.sodium.jni.SodiumJni
import javax.inject.Inject

internal class NitroCryptographyImpl @Inject constructor() : NitroCryptography {
    override fun generateKeyExchangeKeyPair(): KeyExchangeKeyPair {
        val publicKey = ByteArray(SODIUM_PUBLIC_KEY_BYTES)
        val secretKey = ByteArray(SODIUM_SECRET_KEY_BYTES)

        if (!SodiumJni.crypto_kx_keypair(publicKey, secretKey)) {
            throw NitroCryptographyException(message = "Failed to generate key pair")
        }

        return KeyExchangeKeyPair(
            publicKey = publicKey,
            secretKey = secretKey
        )
    }

    override fun initializeSecretStream(
        clientKeyPair: KeyExchangeKeyPair,
        serverInfo: SecretStreamServerInfo
    ): SecretStreamClientInfo {
        val rx = ByteArray(SODIUM_SESSION_KEY_BYTES)
        val tx = ByteArray(SODIUM_SESSION_KEY_BYTES)

        if (!SodiumJni.crypto_kx_client_session_keys(
                rx = rx,
                tx = tx,
                publicKey = clientKeyPair.publicKey,
                secretKey = clientKeyPair.secretKey,
                peerPublicKey = serverInfo.publicKey
            )
        ) {
            throw NitroCryptographyException(message = "Failed to compute shared keys")
        }

        val encryptionState = ByteArray(SODIUM_STATE_BYTES)
        val clientHeader = ByteArray(SODIUM_HEADER_BYTES)

        if (!SodiumJni.crypto_secretstream_xchacha20poly1305_init_push(
                encryptionState,
                clientHeader,
                rx
            )
        ) {
            throw NitroCryptographyException(message = "Failed to initiate encryption stream")
        }

        val decryptionState = ByteArray(SODIUM_STATE_BYTES)

        if (!SodiumJni.crypto_secretstream_xchacha20poly1305_init_pull(
                decryptionState,
                serverInfo.header,
                tx
            )
        ) {
            throw NitroCryptographyException(message = "Failed to initiate encryption stream")
        }

        return SecretStreamClientInfo(
            header = clientHeader,
            states = SecretStreamStates(
                encryptionState = encryptionState,
                decryptionState = decryptionState
            )
        )
    }

    override fun encryptSecretStream(
        states: SecretStreamStates,
        message: ByteArray
    ): ByteArray {
        val encrypted = ByteArray(message.size + ENCRYPTED_EXTRA_BYTES)

        if (!SodiumJni.crypto_secretstream_xchacha20poly1305_push(
                state = states.encryptionState,
                message = message,
                encrypted = encrypted
            )
        ) {
            throw NitroCryptographyException(message = "Failed to encrypt")
        }
        return encrypted
    }

    override fun decryptSecretStream(
        states: SecretStreamStates,
        encrypted: ByteArray
    ): ByteArray {
        val message = ByteArray(encrypted.size - ENCRYPTED_EXTRA_BYTES)

        if (!SodiumJni.crypto_secretstream_xchacha20poly1305_pull(
                state = states.decryptionState,
                message = message,
                encrypted = encrypted
            )
        ) {
            throw NitroCryptographyException(message = "Failed to decrypt")
        }
        return message
    }

    companion object {
        private const val SODIUM_PUBLIC_KEY_BYTES = 32
        private const val SODIUM_SECRET_KEY_BYTES = 32
        private const val SODIUM_SESSION_KEY_BYTES = 32
        private const val SODIUM_STATE_BYTES = 52
        private const val SODIUM_HEADER_BYTES = 24
        private const val ENCRYPTED_EXTRA_BYTES = 17
    }
}