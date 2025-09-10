package com.dashlane.cryptography.jni

const val LIBRARY_CRYPTOGRAPHY = "cryptography"

@Suppress(
    "FunctionParameterNaming",
    "kotlin:S117",
    "Deprecated",
)
internal object JniWrapperCryptography {
    init {
        System.loadLibrary(LIBRARY_CRYPTOGRAPHY)
    }

    
    external fun deriveKeyArgon2d(
        key: ByteArray,
        salt: ByteArray,
        t_cost: Int,
        m_cost: Int,
        parallelism: Int,
        derivedKeySize: Int,
    ): ByteArray

    
    external fun deriveKeyArgon2i(
        key: ByteArray,
        salt: ByteArray,
        t_cost: Int,
        m_cost: Int,
        parallelism: Int,
        derivedKeySize: Int,
    ): ByteArray

    
    external fun deriveKeyPBKDF2sha256(
        key: ByteArray,
        salt: ByteArray,
        iterationCount: Int,
        derivedKeySize: Int,
    ): ByteArray

    
    external fun deriveKeyPBKDF2sha1(
        key: ByteArray,
        salt: ByteArray,
        iterationCount: Int,
        derivedKeySize: Int,
    ): ByteArray

    
    external fun mutateKeyKWC3(key: ByteArray): ByteArray

    external fun encryptContentRSA(data: ByteArray, key: String): ByteArray?

    external fun decryptContentRSA(data: ByteArray, key: String): ByteArray?

    external fun decryptContentRsaPkcs1OaepPadding(data: ByteArray, key: String): ByteArray?

    external fun signContentRSA(data: ByteArray, privateKey: String): ByteArray?

    external fun verifySignatureRSA(
        plaintextContent: ByteArray,
        signedContent: ByteArray,
        publicKey: String,
    ): Boolean

    external fun getKeyForAppData(): String

    external fun generateRSAKeyPair(): Array<String>

    external fun generateX25519KeyPair(): Array<String>

    external fun deriveX25519SharedSecret(
        privateKey: String,
        peerPublicKey: String,
        salt: ByteArray,
        sharedInfo: ByteArray,
        derivedKeySize: Int,
    ): ByteArray?
}