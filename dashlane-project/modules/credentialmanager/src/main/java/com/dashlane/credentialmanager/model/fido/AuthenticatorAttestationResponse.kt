package com.dashlane.credentialmanager.model.fido

import com.dashlane.credentialmanager.model.PasskeyCreationOptions
import com.dashlane.credentialmanager.model.b64Encode
import com.upokecenter.cbor.CBOREncodeOptions
import com.upokecenter.cbor.CBORObject
import java.security.MessageDigest
import java.security.PublicKey
import org.json.JSONArray
import org.json.JSONObject

class AuthenticatorAttestationResponse(
    private val requestOptions: PasskeyCreationOptions,
    private val credentialId: ByteArray,
    private val credentialPublicKey: ByteArray,
    private val publicKey: PublicKey,
    origin: String,
    private val publicKeyAlgorithm: Long,
    packageName: String? = null,
    private val clientDataHash: ByteArray? = null
) : AuthenticatorResponse {
    override var clientJson = JSONObject()
    private val attestationObject: ByteArray

    init {
        clientJson.put("type", "webauthn.create")
        clientJson.put("challenge", requestOptions.challenge)
        clientJson.put("origin", origin)
        if (packageName != null) {
            clientJson.put("androidPackageName", packageName)
        }

        attestationObject = defaultAttestationObject()
    }

    private fun authData(): ByteArray {
        val md = MessageDigest.getInstance("SHA-256")
        val rpHash = md.digest(requestOptions.rp.id.toByteArray())
        val flags = AuthenticatorFlags.USER_PRESENT or
            AuthenticatorFlags.USER_VERIFIED or
            AuthenticatorFlags.BACKUP_ELIGIBILITY or
            AuthenticatorFlags.BACKUP_STATE or
            AuthenticatorFlags.ATTESTED_CRED_DATA_INCLUDED

        val aaguid = getAaguidForWebsite(requestOptions.rp.id)
        val credIdLen = byteArrayOf((credentialId.size shr 8).toByte(), credentialId.size.toByte())

        return rpHash +
            byteArrayOf(flags.toByte()) +
            byteArrayOf(0, 0, 0, 0) +
            aaguid +
            credIdLen +
            credentialId +
            credentialPublicKey
    }

    private fun getAaguidForWebsite(website: String): ByteArray {
        val isBannedWebsite = BANNED_AAGUID_WEBSITES.any {
            website == it || website.endsWith(it)
        }
        if (isBannedWebsite) {
            return ByteArray(16) { 0 }
        }
        return hexStringToByteArray(DASHLANE_AAGUID)
    }

    private fun hexStringToByteArray(hexString: String): ByteArray {
        val hexValues = hexString.split(":")
        return hexValues.map { it.toInt(16).toByte() }.toByteArray()
    }

    private fun defaultAttestationObject(): ByteArray {
        val ao = mutableMapOf<String, Any>()
        ao["fmt"] = "none"
        ao["attStmt"] = emptyMap<Any, Any>()
        ao["authData"] = authData()
        return CBORObject.FromObject(ao).EncodeToBytes(CBOREncodeOptions.DefaultCtap2Canonical)
    }

    override fun json(): JSONObject {
        val clientData = clientJson.toString().toByteArray()
        val response = JSONObject()
        if (clientDataHash == null) {
            response.put("clientDataJSON", b64Encode(clientData))
        }
        response.put("publicKey", b64Encode(publicKey.encoded))
        response.put("authenticatorData", b64Encode(authData()))
        response.put("publicKeyAlgorithm", publicKeyAlgorithm)
        response.put("attestationObject", b64Encode(attestationObject))
        response.put("transports", JSONArray(listOf("internal", "hybrid")))
        return response
    }

    companion object {
        private const val DASHLANE_AAGUID = "53:11:26:d6:e7:17:41:5c:93:20:3d:9a:a6:98:12:39"

        
        private val BANNED_AAGUID_WEBSITES = listOf<String>()
    }
}