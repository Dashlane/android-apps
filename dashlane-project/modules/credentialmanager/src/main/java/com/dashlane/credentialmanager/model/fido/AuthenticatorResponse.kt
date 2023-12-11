package com.dashlane.credentialmanager.model.fido

import org.json.JSONObject

interface AuthenticatorResponse {
    var clientJson: JSONObject
    fun json(): JSONObject
}