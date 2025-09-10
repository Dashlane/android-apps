package com.dashlane.credentialmanager.model.fido.response

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.credentials.CreatePublicKeyCredentialRequest
import com.dashlane.credentialmanager.model.b64Encode
import com.dashlane.credentialmanager.model.fido.AuthenticatorFlags
import com.dashlane.credentialmanager.model.fido.common.ClientJson
import com.dashlane.credentialmanager.model.fido.request.PasskeyCreationOptions
import com.dashlane.passkeys.algorithm.PasskeyAlgorithm
import com.dashlane.server.api.endpoints.passkeys.RegisterPasskeyService
import com.upokecenter.cbor.CBOREncodeOptions
import com.upokecenter.cbor.CBORObject
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.security.MessageDigest
import java.security.PublicKey

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
data class AuthenticatorAttestationResponse(
    @EncodeDefault @SerialName("clientDataJSON")
    val b64ClientDataJson: String? = null,
    @SerialName("publicKey")
    val b64EncodedPublicKey: String,
    @SerialName("authenticatorData")
    val authenticatorData: String,
    @SerialName("publicKeyAlgorithm")
    val publicKeyAlgorithm: Long,
    @SerialName("attestationObject")
    val attestationObject: String,
    @SerialName("transports")
    val transports: List<String>,
) : AuthenticatorResponse {
    companion object {
        private const val DASHLANE_AAGUID = "53:11:26:d6:e7:17:41:5c:93:20:3d:9a:a6:98:12:39"

        
        private val BANNED_AAGUID_WEBSITES = listOf<String>()

        private fun hexStringToByteArray(hexString: String): ByteArray {
            val hexValues = hexString.split(":")
            return hexValues.map { it.toInt(16).toByte() }.toByteArray()
        }

        private fun getAaguidForWebsite(website: String): ByteArray {
            val isBannedWebsite = BANNED_AAGUID_WEBSITES.any {
                website == it || website.endsWith(it)
            }
            if (isBannedWebsite) {
                return ByteArray(16)
            }
            return hexStringToByteArray(DASHLANE_AAGUID)
        }

        private fun authData(
            requestOptions: PasskeyCreationOptions,
            rawCredentialId: ByteArray,
            credentialPublicKey: ByteArray,
        ): ByteArray {
            val md = MessageDigest.getInstance("SHA-256")
            val rpHash = md.digest(requestOptions.rp.id.toByteArray())
            val flags = AuthenticatorFlags.USER_PRESENT or
                AuthenticatorFlags.USER_VERIFIED or
                AuthenticatorFlags.BACKUP_ELIGIBILITY or
                AuthenticatorFlags.BACKUP_STATE or
                AuthenticatorFlags.ATTESTED_CRED_DATA_INCLUDED

            val aaguid = getAaguidForWebsite(requestOptions.rp.id)
            val credIdLen = byteArrayOf((rawCredentialId.size shr 8).toByte(), rawCredentialId.size.toByte())

            return rpHash +
                byteArrayOf(flags.toByte()) +
                byteArrayOf(0, 0, 0, 0) +
                aaguid +
                credIdLen +
                rawCredentialId +
                credentialPublicKey
        }

        private fun defaultAttestationObject(
            requestOptions: PasskeyCreationOptions,
            rawCredentialId: ByteArray,
            credentialPublicKey: ByteArray,
        ): ByteArray {
            val ao = mutableMapOf<String, Any>()
            ao["fmt"] = "none"
            ao["attStmt"] = emptyMap<Any, Any>()
            ao["authData"] = authData(
                requestOptions = requestOptions,
                rawCredentialId = rawCredentialId,
                credentialPublicKey = credentialPublicKey,
            )
            return CBORObject.FromObject(ao).EncodeToBytes(CBOREncodeOptions.DefaultCtap2Canonical)
        }

        fun fromLocalPasskey(
            clientDataHash: ByteArray?,
            publicKey: PublicKey,
            publicKeyAlgorithm: Long,
            clientJson: ClientJson,
            requestOptions: PasskeyCreationOptions,
            rawCredentialId: ByteArray,
            credentialPublicKey: ByteArray,
        ): AuthenticatorAttestationResponse {
            return AuthenticatorAttestationResponse(
                b64ClientDataJson = if (clientDataHash == null) {
                    b64Encode(Json.encodeToString(clientJson).toByteArray())
                } else {
                    null
                },
                b64EncodedPublicKey = b64Encode(publicKey.encoded),
                authenticatorData = b64Encode(
                    authData(
                        requestOptions = requestOptions,
                        rawCredentialId = rawCredentialId,
                        credentialPublicKey = credentialPublicKey,
                    ),
                ),
                publicKeyAlgorithm = publicKeyAlgorithm,
                attestationObject = b64Encode(
                    defaultAttestationObject(
                        requestOptions = requestOptions,
                        rawCredentialId = rawCredentialId,
                        credentialPublicKey = credentialPublicKey,
                    ),
                ),
                transports = listOf("internal", "hybrid"),
            )
        }

        fun fromRegisterData(
            registerData: RegisterPasskeyService.Data.CredentialRegisterData,
            passkeyAlgorithm: PasskeyAlgorithm.Local,
            clientJson: ClientJson,
            request: CreatePublicKeyCredentialRequest,
        ): AuthenticatorAttestationResponse {
            val publicKey = passkeyAlgorithm.convertNitroKeyToPublicKey(registerData.publicKey)
            return AuthenticatorAttestationResponse(
                b64ClientDataJson = if (request.clientDataHash == null) {
                    b64Encode(Json.encodeToString(clientJson).toByteArray())
                } else {
                    null
                },
                b64EncodedPublicKey = b64Encode(publicKey.encoded),
                authenticatorData = registerData.authenticatorData,
                publicKeyAlgorithm = registerData.publicKeyAlgorithm,
                attestationObject = registerData.attestationObject,
                transports = registerData.transports,
            )
        }
    }
}