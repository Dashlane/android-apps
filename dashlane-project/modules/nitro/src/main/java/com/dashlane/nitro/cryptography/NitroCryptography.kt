package com.dashlane.nitro.cryptography

internal interface NitroCryptography {
    fun generateKeyExchangeKeyPair(): KeyExchangeKeyPair

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