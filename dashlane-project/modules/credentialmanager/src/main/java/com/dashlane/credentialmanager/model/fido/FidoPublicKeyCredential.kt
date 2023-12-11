package com.dashlane.credentialmanager.model.fido

import com.dashlane.credentialmanager.model.b64Encode
import org.json.JSONObject

class FidoPublicKeyCredential(
    val rawId: ByteArray,
    val response: AuthenticatorResponse
) {
    fun json(): String {
        val encodedId = b64Encode(rawId)
        val ret = JSONObject()
        ret.put("id", encodedId)
        ret.put("rawId", encodedId)
        ret.put("type", "public-key")
        ret.put("authenticatorAttachment", "platform")
        ret.put("response", response.json())
        ret.put("clientExtensionResults", JSONObject())
        return ret.toString()
    }
}