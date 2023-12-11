package com.dashlane.credentialmanager.model.fido

import com.dashlane.credentialmanager.model.PasskeyRequestOptions
import com.dashlane.credentialmanager.model.b64Encode
import java.security.MessageDigest
import org.json.JSONObject

class AuthenticatorAssertionResponse(
    origin: String,
    packageName: String? = null,
    private val requestOptions: PasskeyRequestOptions,
    private var userHandle: ByteArray,
    private val clientDataHash: ByteArray? = null,
) : AuthenticatorResponse {
    override var clientJson = JSONObject()
    var authenticatorData: ByteArray
    var signature: ByteArray = byteArrayOf()

    init {
        clientJson.put("type", "webauthn.get")
        clientJson.put("challenge", requestOptions.challenge)
        clientJson.put("origin", origin)
        if (packageName != null) {
            clientJson.put("androidPackageName", packageName)
        }
        authenticatorData = defaultAuthenticatorData()
    }

    private fun defaultAuthenticatorData(): ByteArray {
        val md = MessageDigest.getInstance("SHA-256")
        val rpHash = md.digest(requestOptions.rpId.toByteArray())
        val flags = AuthenticatorFlags.USER_PRESENT or
                AuthenticatorFlags.USER_VERIFIED or
                AuthenticatorFlags.BACKUP_ELIGIBILITY or
                AuthenticatorFlags.BACKUP_STATE
        return rpHash + byteArrayOf(flags.toByte()) + byteArrayOf(0, 0, 0, 0)
    }

    fun dataToSign(): ByteArray {
        val md = MessageDigest.getInstance("SHA-256")
        val hash = clientDataHash ?: md.digest(clientJson.toString().toByteArray())
        return authenticatorData + hash
    }

    override fun json(): JSONObject {
        val clientData = clientJson.toString().toByteArray()
        val response = JSONObject()
        if (clientDataHash == null) {
            response.put("clientDataJSON", b64Encode(clientData))
        }
        response.put("authenticatorData", b64Encode(authenticatorData))
        response.put("signature", b64Encode(signature))
        response.put("userHandle", b64Encode(userHandle))
        return response
    }
}