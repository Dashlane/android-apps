package com.dashlane.passkeys.algorithm

import com.dashlane.credentialmanager.model.CloudPasskeyInvalidPublicKeyException
import com.dashlane.credentialmanager.model.b64Decode
import com.dashlane.passkeys.algorithm.PasskeyAlgorithm.Cloud.algorithmIdentifier
import com.dashlane.util.stackTraceToSafeString
import com.nimbusds.jose.jwk.Curve
import com.nimbusds.jose.jwk.ECParameterTable
import com.upokecenter.cbor.CBOREncodeOptions
import com.upokecenter.cbor.CBORObject
import java.math.BigInteger
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.PublicKey
import java.security.Signature
import java.security.interfaces.ECPublicKey
import java.security.spec.ECGenParameterSpec
import java.security.spec.ECPoint
import java.security.spec.ECPublicKeySpec

sealed class PasskeyAlgorithm {
    abstract val algorithmIdentifier: Long

    sealed class Local : PasskeyAlgorithm() {
        abstract fun createKeyPair(): KeyPair

        abstract fun signChallenge(challenge: ByteArray, privateKey: PrivateKey): ByteArray

        abstract fun encodePublicKey(key: ECPublicKey): ByteArray

        @Throws(CloudPasskeyInvalidPublicKeyException::class)
        abstract fun convertNitroKeyToPublicKey(nitroPublicKey: String): PublicKey

        data object ES256 : Local() {
            override val algorithmIdentifier: Long
                get() = -7

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
                coseKey[3] = this.algorithmIdentifier
                coseKey[-1] = 1 
                coseKey[-2] = x 
                coseKey[-3] = y 
                return CBORObject.FromObject(coseKey).EncodeToBytes(CBOREncodeOptions.DefaultCtap2Canonical)
            }

            @Throws(CloudPasskeyInvalidPublicKeyException::class)
            override fun convertNitroKeyToPublicKey(nitroPublicKey: String): PublicKey {
                return try {
                    
                    val cbor = CBORObject.DecodeFromBytes(b64Decode(nitroPublicKey))
                    val ecParameters = ECParameterTable.get(Curve.P_256)
                    val x = BigInteger(1, cbor[-2].GetByteString())
                    val y = BigInteger(1, cbor[-3].GetByteString())
                    val ecPoint = ECPoint(x, y)
                    val keySpec = ECPublicKeySpec(ecPoint, ecParameters)
                    val keyFactory = KeyFactory.getInstance("EC")
                    keyFactory.generatePublic(keySpec)
                } catch (e: Exception) {
                    throw CloudPasskeyInvalidPublicKeyException(e.stackTraceToSafeString())
                }
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
    }

    data object Cloud : PasskeyAlgorithm() {
        override val algorithmIdentifier: Long
            get() = -65537L
    }

    companion object {
        fun provider(algorithmValue: Long): PasskeyAlgorithm? {
            return when (algorithmValue) {
                Local.ES256.algorithmIdentifier -> Local.ES256
                Cloud.algorithmIdentifier -> Cloud
                else -> null
            }
        }
    }
}