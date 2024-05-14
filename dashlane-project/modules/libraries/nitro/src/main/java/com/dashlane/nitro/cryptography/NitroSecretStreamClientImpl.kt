package com.dashlane.nitro.cryptography

import com.dashlane.nitro.NitroCryptographyException
import com.dashlane.nitro.cryptography.sodium.SodiumCryptography
import com.dashlane.nitro.cryptography.sodium.SodiumCryptography.Companion.ENCRYPTED_EXTRA_BYTES
import com.dashlane.nitro.cryptography.sodium.SodiumCryptography.Companion.SODIUM_HEADER_BYTES
import com.dashlane.nitro.cryptography.sodium.SodiumCryptography.Companion.SODIUM_STATE_BYTES
import com.dashlane.nitro.cryptography.sodium.keys.KeyExchangeKeyPair
import javax.inject.Inject

internal class NitroSecretStreamClientImpl @Inject constructor(private val sodiumCryptography: SodiumCryptography) :
    NitroSecretStreamClient {

    override fun initializeSecretStream(
        clientKeyPair: KeyExchangeKeyPair,
        serverInfo: SecretStreamServerInfo
    ): SecretStreamClientInfo {
        val sharedKeyPair = sodiumCryptography.keyExchangeClientSessionKeys(
            clientKeyPair = clientKeyPair,
            serverPublicKey = serverInfo.publicKey
        ) ?: throw NitroCryptographyException(message = "Failed to compute shared keys")

        val encryptionState = ByteArray(SODIUM_STATE_BYTES)
        val clientHeader = ByteArray(SODIUM_HEADER_BYTES)

        if (!sodiumCryptography.secretstreamXchacha20poly1305InitPush(
                state = encryptionState,
                header = clientHeader,
                key = sharedKeyPair.rx
            )
        ) {
            throw NitroCryptographyException(message = "Failed to initiate encryption stream")
        }

        val decryptionState = ByteArray(SODIUM_STATE_BYTES)

        if (!sodiumCryptography.secretstreamXchacha20poly1305InitPull(
                state = decryptionState,
                header = serverInfo.header,
                key = sharedKeyPair.tx
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

        if (!sodiumCryptography.secretstreamXchacha20poly1305Push(
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

        if (!sodiumCryptography.secretstreamXchacha20poly1305Pull(
                state = states.decryptionState,
                message = message,
                encrypted = encrypted
            )
        ) {
            throw NitroCryptographyException(message = "Failed to decrypt")
        }
        return message
    }
}