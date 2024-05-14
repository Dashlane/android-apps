package com.dashlane.nitro.cryptography

import com.dashlane.nitro.cryptography.sodium.keys.KeyExchangeKeyPair

internal interface NitroSecretStreamClient {

    fun initializeSecretStream(
        clientKeyPair: KeyExchangeKeyPair,
        serverInfo: SecretStreamServerInfo
    ): SecretStreamClientInfo

    fun encryptSecretStream(
        states: SecretStreamStates,
        message: ByteArray
    ): ByteArray

    fun decryptSecretStream(
        states: SecretStreamStates,
        encrypted: ByteArray
    ): ByteArray
}