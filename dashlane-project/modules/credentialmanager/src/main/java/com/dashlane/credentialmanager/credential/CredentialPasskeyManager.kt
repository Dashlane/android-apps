package com.dashlane.credentialmanager.credential

import androidx.annotation.RequiresApi
import androidx.credentials.CreatePublicKeyCredentialRequest
import androidx.credentials.CreatePublicKeyCredentialResponse
import androidx.credentials.PublicKeyCredential
import androidx.credentials.provider.CallingAppInfo
import com.dashlane.credentialmanager.CredentialManagerDAO
import com.dashlane.credentialmanager.algorithm.PasskeyAlgorithm
import com.dashlane.credentialmanager.model.DashlaneCredentialManagerException
import com.dashlane.credentialmanager.model.PasskeyCreationOptions
import com.dashlane.credentialmanager.model.PasskeyPrivateKeyException
import com.dashlane.credentialmanager.model.PasskeyRequestOptions
import com.dashlane.credentialmanager.model.PasskeyUnsupportedAlgorithmException
import com.dashlane.credentialmanager.model.appInfoToOrigin
import com.dashlane.credentialmanager.model.b64Decode
import com.dashlane.credentialmanager.model.b64Encode
import com.dashlane.credentialmanager.model.fido.AuthenticatorAssertionResponse
import com.dashlane.credentialmanager.model.fido.AuthenticatorAttestationResponse
import com.dashlane.credentialmanager.model.fido.FidoPublicKeyCredential
import com.dashlane.vault.model.VaultItem
import com.dashlane.xml.domain.SyncObject
import com.google.gson.Gson
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.jwk.Curve
import com.nimbusds.jose.jwk.ECKey
import com.nimbusds.jose.jwk.KeyOperation
import com.nimbusds.jose.jwk.KeyUse
import com.nimbusds.jose.util.Base64URL
import java.security.KeyPair
import java.security.PrivateKey
import java.security.SecureRandom
import java.security.interfaces.ECPublicKey
import java.time.Instant
import java.util.Date
import java.util.UUID
import javax.inject.Inject

interface CredentialPasskeyManager {
    suspend fun createPasskey(
        createPublicKeyRequest: CreatePublicKeyCredentialRequest,
        callingAppInfo: CallingAppInfo
    ): CreatePublicKeyCredentialResponse

    suspend fun providePasskey(
        publicKeyRequest: PasskeyRequestOptions,
        vaultItem: VaultItem<SyncObject.Passkey>,
        callingAppInfo: CallingAppInfo,
        clientDataHash: ByteArray?
    ): PublicKeyCredential
}

@RequiresApi(34)
class CredentialPasskeyManagerImpl @Inject constructor(
    private val databaseAccess: CredentialManagerDAO
) : CredentialPasskeyManager {

    override suspend fun createPasskey(
        createPublicKeyRequest: CreatePublicKeyCredentialRequest,
        callingAppInfo: CallingAppInfo
    ): CreatePublicKeyCredentialResponse {
        
        val credId = ByteArray(32)
        SecureRandom().nextBytes(credId)

        val request = Gson().fromJson(createPublicKeyRequest.requestJson, PasskeyCreationOptions::class.java)

        
        val passkeyAlgorithm = request.pubKeyCredParams.firstNotNullOfOrNull {
            PasskeyAlgorithm.provider(it.alg)
        } ?: throw PasskeyUnsupportedAlgorithmException(
            "Unsupported algorithm ${request.pubKeyCredParams.map { it.alg }.joinToString(", ")}"
        )
        val keyPair = passkeyAlgorithm.createKeyPair()

        
        databaseAccess.savePasskeyCredential(
            counter = 0,
            privateKey = keyPair.toDashlanePrivateKey(),
            credentialId = b64Encode(credId),
            rpName = request.rp.name,
            rpId = request.rp.id,
            userHandle = request.user.id,
            userDisplayName = request.user.displayName,
            keyAlgorithm = passkeyAlgorithm.algorithmIdentifier
        ) ?: throw DashlaneCredentialManagerException("Failed to save the passkey item")

        
        val response = AuthenticatorAttestationResponse(
            requestOptions = request,
            credentialId = credId,
            publicKey = keyPair.public,
            credentialPublicKey = passkeyAlgorithm.encodePublicKey(keyPair.public as ECPublicKey),
            origin = appInfoToOrigin(callingAppInfo),
            publicKeyAlgorithm = passkeyAlgorithm.algorithmIdentifier,
            packageName = callingAppInfo.packageName,
            clientDataHash = createPublicKeyRequest.clientDataHash
        )
        val credential = FidoPublicKeyCredential(
            rawId = credId,
            response = response
        )
        return CreatePublicKeyCredentialResponse(credential.json())
    }

    override suspend fun providePasskey(
        publicKeyRequest: PasskeyRequestOptions,
        vaultItem: VaultItem<SyncObject.Passkey>,
        callingAppInfo: CallingAppInfo,
        clientDataHash: ByteArray?
    ): PublicKeyCredential {
        val syncObject = vaultItem.syncObject
        val response = AuthenticatorAssertionResponse(
            requestOptions = publicKeyRequest,
            origin = appInfoToOrigin(callingAppInfo),
            userHandle = b64Decode(syncObject.userHandle!!),
            packageName = callingAppInfo.packageName,
            clientDataHash = clientDataHash
        )

        
        val counter = syncObject.counter ?: 0
        val counterHasPastIncrements = counter > 0
        val newCounter = if (counterHasPastIncrements) counter + 1 else 0

        
        databaseAccess.updatePasskey(vaultItem.uid, Instant.now(), newCounter)

        
        val privateKey = syncObject.privateKey ?: throw PasskeyPrivateKeyException()

        
        val passkeyAlgorithm = syncObject.keyAlgorithm?.let {
            PasskeyAlgorithm.provider(it)
        } ?: throw PasskeyUnsupportedAlgorithmException("Unsupported algorithms => ${syncObject.keyAlgorithm}")
        response.signature = passkeyAlgorithm.signChallenge(
            response.dataToSign(),
            privateKey.toJavaPrivateKey()
        )

        
        val credential = FidoPublicKeyCredential(
            rawId = b64Decode(syncObject.credentialId!!),
            response = response
        )
        return PublicKeyCredential(credential.json())
    }

    private fun KeyPair.toDashlanePrivateKey(): SyncObject.Passkey.PrivateKey {
        val jwk = ECKey.Builder(Curve.P_256, public as ECPublicKey)
            .privateKey(private)
            .keyUse(KeyUse.SIGNATURE)
            .keyID(UUID.randomUUID().toString())
            .keyOperations(setOf(KeyOperation.VERIFY, KeyOperation.SIGN))
            .issueTime(Date())
            .build()
        return SyncObject.Passkey.PrivateKey.Builder()
            .apply {
                crv = jwk.curve.name
                ext = true
                keyOps = jwk.keyOperations?.map { it.name.lowercase() } ?: listOf()
                kty = jwk.keyType.value
                d = jwk.d.toString()
                x = jwk.x.toString()
                y = jwk.y.toString()
            }
            .build()
    }

    private fun SyncObject.Passkey.PrivateKey.toJavaPrivateKey(): PrivateKey {
        val jwk = ECKey.Builder(Curve(crv), Base64URL(x), Base64URL(y))
            .d(Base64URL(d))
            .algorithm(JWSAlgorithm.EdDSA)
            .keyOperations(setOf(KeyOperation.SIGN))
            .build()
        return jwk.toECPrivateKey()
    }
}