package com.dashlane.cryptography

import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo
import org.bouncycastle.openssl.PEMKeyPair
import org.bouncycastle.openssl.PEMParser
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter
import org.bouncycastle.openssl.jcajce.JcaPEMWriter
import org.bouncycastle.util.io.pem.PemObject
import org.bouncycastle.util.io.pem.PemWriter
import java.io.IOException
import java.io.StringWriter
import java.security.PrivateKey
import java.security.PublicKey

internal fun PublicKey.toSharingPublicKeyValue(): String =
    writeString { stringWriter ->
        PemWriter(stringWriter).use { pemWriter ->
            pemWriter.writeObject(toPkcs1PublicKeyPemObject())
        }
    }

private fun PublicKey.toPkcs1PublicKeyPemObject(): PemObject {
    val publicKeyInfo = SubjectPublicKeyInfo.getInstance(encoded)
    val primitive = publicKeyInfo.parsePublicKey()
    val publicKeyPkcs1 = primitive.encoded
    return PemObject("RSA PUBLIC KEY", publicKeyPkcs1)
}

internal fun PrivateKey.toSharingPrivateKeyValue() =
    writeString { stringWriter ->
        JcaPEMWriter(stringWriter).use { pemWriter ->
            pemWriter.writeObject(this)
        }
    }

private inline fun writeString(block: (StringWriter) -> Unit) =
    StringWriter().apply(block).toString()

internal fun SharingKeys.Public.toPublicKey(): PublicKey {
    val pemObject = readPemObject(value)
    if (pemObject !is SubjectPublicKeyInfo) throw CryptographyException("Expected public key, but was ${pemObject::class}.")
    return JcaPEMKeyConverter().getPublicKey(pemObject)
}

internal fun SharingKeys.Private.toPrivateKey(): PrivateKey {
    val pemObject = readPemObject(value)
    if (pemObject !is PEMKeyPair) throw CryptographyException("Expected private key, but was ${pemObject::class}.")
    return JcaPEMKeyConverter().getPrivateKey(pemObject.privateKeyInfo)
}

private fun readPemObject(s: String) =
    try {
        PEMParser(s.reader()).use { reader -> reader.readObject() }
            ?: throw CryptographyException("Invalid PEM object.")
    } catch (e: IOException) {
        throw CryptographyException("Invalid PEM object.", e)
    }