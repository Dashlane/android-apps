package com.dashlane.credentialmanager.credential

import com.dashlane.xml.domain.SyncObject
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.jwk.Curve
import com.nimbusds.jose.jwk.ECKey
import com.nimbusds.jose.jwk.KeyOperation
import com.nimbusds.jose.jwk.KeyUse
import com.nimbusds.jose.util.Base64URL
import java.security.KeyPair
import java.security.PrivateKey
import java.security.interfaces.ECPublicKey
import java.util.Date
import java.util.UUID

fun KeyPair.toDashlanePrivateKey(): SyncObject.Passkey.PrivateKey {
    val crv = SyncObject.Passkey.Crv.P_256
    val kty = SyncObject.Passkey.Kty.EC
    val jwk = ECKey.Builder(Curve(crv.value), public as ECPublicKey)
        .privateKey(private)
        .keyUse(KeyUse.SIGNATURE)
        .keyID(UUID.randomUUID().toString())
        .keyOperations(setOf(KeyOperation.VERIFY, KeyOperation.SIGN))
        .issueTime(Date()).build()
    return SyncObject.Passkey.PrivateKey.Builder().apply {
        this.crv = crv
        ext = true
        keyOps = jwk.keyOperations?.map { it.name.lowercase() } ?: listOf()
        this.kty = kty
        d = jwk.d.toString()
        x = jwk.x.toString()
        y = jwk.y.toString()
    }.build()
}

fun SyncObject.Passkey.PrivateKey.toJavaPrivateKey(): PrivateKey {
    val jwk =
        ECKey.Builder(Curve(crv?.value), Base64URL(x), Base64URL(y)).d(Base64URL(d)).algorithm(JWSAlgorithm.EdDSA)
            .keyOperations(setOf(KeyOperation.SIGN)).build()
    return jwk.toECPrivateKey()
}