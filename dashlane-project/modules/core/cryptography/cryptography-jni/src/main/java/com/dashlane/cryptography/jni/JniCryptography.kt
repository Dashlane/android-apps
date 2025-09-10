package com.dashlane.cryptography.jni

@Suppress(
    "FunctionParameterNaming",
    "kotlin:S117",
    "kotlin:S1133",
)
class JniCryptography {
    fun deriveKeyArgon2d(
        key: ByteArray,
        salt: ByteArray,
        t_cost: Int,
        m_cost: Int,
        parallelism: Int,
        derivedKeySizeInByte: Int,
    ): ByteArray =
        JniWrapperCryptography.deriveKeyArgon2d(
            key,
            salt,
            t_cost,
            m_cost,
            parallelism,
            derivedKeySizeInByte,
        )

    fun deriveKeyArgon2i(
        key: ByteArray,
        salt: ByteArray,
        t_cost: Int,
        m_cost: Int,
        parallelism: Int,
        derivedKeySizeInByte: Int,
    ): ByteArray =
        JniWrapperCryptography.deriveKeyArgon2i(
            key,
            salt,
            t_cost,
            m_cost,
            parallelism,
            derivedKeySizeInByte,
        )

    fun deriveKeyPBKDF2sha256(
        key: ByteArray,
        salt: ByteArray,
        iterationCount: Int,
        derivedKeySizeInByte: Int,
    ): ByteArray =
        JniWrapperCryptography.deriveKeyPBKDF2sha256(
            key,
            salt,
            iterationCount,
            derivedKeySizeInByte,
        )

    fun deriveKeyPBKDF2sha1(
        key: ByteArray,
        salt: ByteArray,
        iterationCount: Int,
        derivedKeySizeInByte: Int,
    ): ByteArray =
        JniWrapperCryptography.deriveKeyPBKDF2sha1(key, salt, iterationCount, derivedKeySizeInByte)

    @Deprecated(
        "KWC3 will use mutated key for compatibility",
        ReplaceWith("DO NOT use mutate key for any other cryptography"),
        DeprecationLevel.WARNING,
    )
    fun mutateKeyKWC3(key: ByteArray): ByteArray = JniWrapperCryptography.mutateKeyKWC3(key)

    fun encryptContentRSA(data: ByteArray, key: String): ByteArray? =
        JniWrapperCryptography.encryptContentRSA(data, key)

    fun decryptContentRSA(data: ByteArray, key: String): ByteArray? =
        JniWrapperCryptography.decryptContentRSA(data, key)

    fun decryptContentRsaPkcs1OaepPadding(data: ByteArray, key: String): ByteArray? =
        JniWrapperCryptography.decryptContentRsaPkcs1OaepPadding(data, key)

    fun signContentRSA(data: ByteArray, privateKey: String): ByteArray? =
        JniWrapperCryptography.signContentRSA(data, privateKey)

    fun verifySignatureRSA(
        plaintextContent: ByteArray,
        signedContent: ByteArray,
        publicKey: String,
    ): Boolean =
        JniWrapperCryptography.verifySignatureRSA(plaintextContent, signedContent, publicKey)

    fun getKeyForAppData(): String = JniWrapperCryptography.getKeyForAppData()

    fun generateRSAKeyPair(): Array<String> = JniWrapperCryptography.generateRSAKeyPair()

    fun generateX25519KeyPair(): Array<String> = JniWrapperCryptography.generateX25519KeyPair()

    fun deriveX25519SharedSecret(
        privateKey: String,
        peerPublicKey: String,
        salt: ByteArray,
        sharedInfo: ByteArray,
        derivedKeySize: Int,
    ): ByteArray? = JniWrapperCryptography.deriveX25519SharedSecret(
        privateKey,
        peerPublicKey,
        salt,
        sharedInfo,
        derivedKeySize,
    )
}