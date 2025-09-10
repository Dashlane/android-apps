package com.dashlane.cryptography

import java.security.Key
import java.security.NoSuchAlgorithmException
import java.security.spec.AlgorithmParameterSpec
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

@Suppress("FunctionName")
fun CipherFactory(): CipherFactory =
    CipherFactoryImpl()

private const val AES = "AES"
private const val AES_CBC_PKCS5PADDING = "AES/CBC/PKCS5Padding"
private const val AES_CBC_PKCS7PADDING = "AES/CBC/PKCS7Padding"

private class CipherFactoryImpl : CipherFactory {
    override fun createAesCbcCipher(
        mode: CipherFactory.Mode,
        key: ByteArray,
        iv: ByteArray,
    ): Cipher =
        createCipher().apply {
            init(
                mode.asCipherMode(),
                key.asAesKey(),
                iv.asIvParameter(),
            )
        }

    private fun createCipher() =
        try {
            Cipher.getInstance(AES_CBC_PKCS7PADDING)
        } catch (e1: NoSuchAlgorithmException) {
            try {
                Cipher.getInstance(AES_CBC_PKCS5PADDING)
            } catch (e2: NoSuchAlgorithmException) {
                e1.addSuppressed(e1)
                throw e1
            }
        }
}

private fun CipherFactory.Mode.asCipherMode(): Int = when (this) {
    CipherFactory.Mode.ENCRYPT -> Cipher.ENCRYPT_MODE
    CipherFactory.Mode.DECRYPT -> Cipher.DECRYPT_MODE
}

private fun ByteArray.asAesKey(): Key =
    SecretKeySpec(this, AES)

private fun ByteArray.asIvParameter(): AlgorithmParameterSpec =
    IvParameterSpec(this)