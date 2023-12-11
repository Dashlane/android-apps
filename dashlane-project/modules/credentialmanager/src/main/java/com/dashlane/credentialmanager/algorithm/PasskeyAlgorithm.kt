package com.dashlane.credentialmanager.algorithm

import com.upokecenter.cbor.CBOREncodeOptions
import com.upokecenter.cbor.CBORObject
import java.math.BigInteger
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.Signature
import java.security.interfaces.ECPublicKey
import java.security.spec.ECGenParameterSpec

interface PasskeyAlgorithm {
    val algorithmIdentifier: Long
    fun createKeyPair(): KeyPair
    fun signChallenge(challenge: ByteArray, privateKey: PrivateKey): ByteArray
    fun encodePublicKey(key: ECPublicKey): ByteArray

    companion object {
        fun provider(algorithmValue: Long): PasskeyAlgorithm? {
            return when (algorithmValue) {
                -7L -> PasskeyAlgorithmES256()
                else -> null
            }
        }
    }
}

data class PasskeyAlgorithmES256(override val algorithmIdentifier: Long = -7) : PasskeyAlgorithm {

    override fun createKeyPair(): KeyPair {
        val spec = ECGenParameterSpec("secp256r1")
        val keyPairGen = KeyPairGenerator.getInstance("EC")
        keyPairGen.initialize(spec)
        return keyPairGen.genKeyPair()
    }

    override fun signChallenge(challenge: ByteArray, privateKey: PrivateKey): ByteArray {
        val sig = Signature.getInstance("SHA256withECDSA")
        sig.initSign(privateKey)
        sig.update(challenge)
        return sig.sign()
    }

    override fun encodePublicKey(key: ECPublicKey): ByteArray {
        val x = bigIntToFixedArray(key.w.affineX, 32)
        val y = bigIntToFixedArray(key.w.affineY, 32)
        val coseKey = mutableMapOf<Int, Any>()
        coseKey[1] = 2 
        coseKey[3] = -7 
        coseKey[-1] = 1 
        coseKey[-2] = x 
        coseKey[-3] = y 
        return CBORObject.FromObject(coseKey).EncodeToBytes(CBOREncodeOptions.DefaultCtap2Canonical)
    }

    private fun bigIntToFixedArray(n: BigInteger, size: Int): ByteArray {
        assert(n.signum() >= 0)

        val bytes = n.toByteArray()
        
        
        var offset = 0
        if (bytes[0] == 0x00.toByte()) {
            offset++
        }
        val bytesLen = bytes.size - offset
        assert(bytesLen <= size)

        val output = ByteArray(size)
        System.arraycopy(bytes, offset, output, size - bytesLen, bytesLen)
        return output
    }
}