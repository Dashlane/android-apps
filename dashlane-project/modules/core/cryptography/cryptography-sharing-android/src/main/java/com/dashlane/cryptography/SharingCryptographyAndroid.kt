package com.dashlane.cryptography

import com.dashlane.cryptography.jni.JniCryptography

@Suppress("FunctionName")
fun SharingCryptography(
    jniCryptography: JniCryptography = JniCryptography(),
): SharingCryptography = SharingCryptographyImpl(
    jniCryptography,
)

private class SharingCryptographyImpl(
    private val jniCryptography: JniCryptography,
) : SharingCryptography {
    override fun encryptRsa(data: ByteArray, publicKey: SharingKeys.Public) =
        (jniCryptography.encryptContentRSA(data, publicKey.value) ?: throw CryptographyException()).asSharingEncryptedByteArray()

    override fun decryptRsa(data: SharingEncryptedByteArray, privateKey: SharingKeys.Private) =
        jniCryptography.decryptContentRSA(data.value, privateKey.value) ?: throw CryptographyException()

    override fun decryptRsaPkcs1OaepPadding(data: SharingEncryptedByteArray, privateKey: SharingKeys.Private) =
        jniCryptography.decryptContentRsaPkcs1OaepPadding(data.value, privateKey.value) ?: throw CryptographyException()

    override fun signRsa(data: ByteArray, privateKey: SharingKeys.Private) =
        (jniCryptography.signContentRSA(data, privateKey.value) ?: throw CryptographyException()).asSharingSignatureByteArray()

    override fun verifySignatureRsa(
        data: ByteArray,
        signature: SharingSignatureByteArray,
        publicKey: SharingKeys.Public,
    ): Boolean =
        jniCryptography.verifySignatureRSA(data, signature.value, publicKey.value)

    override fun generateSharingKeys(): SharingKeys {
        val keyPairArray = jniCryptography.generateRSAKeyPair()

        if (keyPairArray.size != 2) throw CryptographyException("Expected key pair size 2, but was ${keyPairArray.size}.")
        val publicKey = keyPairArray[0].ifEmpty { null } ?: throw CryptographyException("Empty public key.")
        val privateKey = keyPairArray[1].ifEmpty { null } ?: throw CryptographyException("Empty private key.")

        return SharingKeys(publicKey, privateKey)
    }
}