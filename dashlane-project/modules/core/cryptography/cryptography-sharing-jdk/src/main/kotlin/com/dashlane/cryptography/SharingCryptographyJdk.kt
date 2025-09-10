package com.dashlane.cryptography

import java.security.Key
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.SecureRandom
import java.security.Signature
import java.security.SignatureException
import java.security.spec.MGF1ParameterSpec
import javax.crypto.BadPaddingException
import javax.crypto.Cipher
import javax.crypto.IllegalBlockSizeException
import javax.crypto.spec.OAEPParameterSpec
import javax.crypto.spec.PSource

@Suppress("FunctionName")
fun SharingCryptography(
    random: SecureRandom = SecureRandom(),
): SharingCryptography = SharingCryptographyImpl(
    random,
)

private class SharingCryptographyImpl(
    private val random: SecureRandom,
) : SharingCryptography {
    override fun encryptRsa(data: ByteArray, publicKey: SharingKeys.Public): SharingEncryptedByteArray {
        val rsaPublicKey = publicKey.toPublicKey()
        val cipher = createRsaCipher(Cipher.ENCRYPT_MODE, rsaPublicKey)
        try {
            return cipher.doFinal(data).asSharingEncryptedByteArray()
        } catch (e: IllegalBlockSizeException) {
            throw CryptographyException(cause = e)
        } catch (e: BadPaddingException) {
            throw CryptographyException(cause = e)
        }
    }

    override fun decryptRsa(data: SharingEncryptedByteArray, privateKey: SharingKeys.Private): ByteArray {
        val rsaPrivateKey = privateKey.toPrivateKey()
        val cipher = createRsaCipher(Cipher.DECRYPT_MODE, rsaPrivateKey)
        try {
            return cipher.doFinal(data.value)
        } catch (e: IllegalBlockSizeException) {
            throw CryptographyException(cause = e)
        } catch (e: BadPaddingException) {
            throw CryptographyException(cause = e)
        }
    }

    private fun createRsaCipher(mode: Int, key: Key): Cipher {
        val cipher = createRsaCipher()
        val oaepSpec = OAEPParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256, PSource.PSpecified.DEFAULT)
        cipher.init(mode, key, oaepSpec, random)
        return cipher
    }

    override fun decryptRsaPkcs1OaepPadding(data: SharingEncryptedByteArray, privateKey: SharingKeys.Private): ByteArray {
        val rsaPrivateKey = privateKey.toPrivateKey()
        val cipher = createRsaPkcs1OaepPaddingCipher(Cipher.DECRYPT_MODE, rsaPrivateKey)
        try {
            return cipher.doFinal(data.value)
        } catch (e: IllegalBlockSizeException) {
            throw CryptographyException(cause = e)
        } catch (e: BadPaddingException) {
            throw CryptographyException(cause = e)
        }
    }

    private fun createRsaPkcs1OaepPaddingCipher(mode: Int, key: Key): Cipher {
        val cipher = createRsaCipher()
        cipher.init(mode, key, random)
        return cipher
    }

    private fun createRsaCipher() =
        Cipher.getInstance("RSA/ECB/OAEPPadding")

    override fun signRsa(data: ByteArray, privateKey: SharingKeys.Private): SharingSignatureByteArray {
        val privateSignature = createRsaSignature()
        privateSignature.initSign(privateKey.toPrivateKey(), random)
        privateSignature.update(data)
        return privateSignature.sign().asSharingSignatureByteArray()
    }

    override fun verifySignatureRsa(data: ByteArray, signature: SharingSignatureByteArray, publicKey: SharingKeys.Public): Boolean {
        val publicSignature = createRsaSignature()
        publicSignature.initVerify(publicKey.toPublicKey())
        publicSignature.update(data)
        try {
            return publicSignature.verify(signature.value)
        } catch (e: SignatureException) {
            throw CryptographyException(cause = e)
        }
    }

    private fun createRsaSignature() =
        Signature.getInstance("SHA256withRSA")

    override fun generateSharingKeys(): SharingKeys {
        val keyPair = generateKeyPair(random)

        val publicKey = keyPair.public.toSharingPublicKeyValue()
        val privateKey = keyPair.private.toSharingPrivateKeyValue()

        return SharingKeys(publicKey, privateKey)
    }
}

private const val RSA_KEY_SIZE = 2048

private fun generateKeyPair(random: SecureRandom): KeyPair {
    val keyPairGenerator = createRsaKeyPairGenerator()
    keyPairGenerator.initialize(RSA_KEY_SIZE, random)
    return keyPairGenerator.generateKeyPair()
}

private fun createRsaKeyPairGenerator() =
    KeyPairGenerator.getInstance("RSA")