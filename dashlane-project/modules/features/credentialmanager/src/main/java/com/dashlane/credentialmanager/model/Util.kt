package com.dashlane.credentialmanager.model

import androidx.annotation.RequiresApi
import androidx.credentials.provider.CallingAppInfo
import com.dashlane.credentialmanager.model.fido.common.ClientJson
import com.dashlane.credentialmanager.model.fido.common.RequestType
import kotlinx.serialization.json.Json
import java.security.MessageDigest
import java.util.Base64

fun b64Decode(str: String): ByteArray {
    return Base64.getUrlDecoder().decode(str)
}

fun b64Encode(data: ByteArray): String {
    return Base64.getUrlEncoder().withoutPadding().encodeToString(data)
}

@RequiresApi(34)
fun appInfoToOrigin(info: CallingAppInfo): String {
    val cert = info.signingInfo.apkContentsSigners[0].toByteArray()
    val md = MessageDigest.getInstance("SHA-256")
    val certHash = md.digest(cert)
    return "android:apk-key-hash:${b64Encode(certHash)}"
}

@RequiresApi(34)
fun getB64HashForRequest(
    type: RequestType,
    challenge: String,
    callingAppInfo: CallingAppInfo,
    clientDataHash: ByteArray?,
): String {
    val dataHash = if (clientDataHash != null) {
        clientDataHash
    } else {
        val md = MessageDigest.getInstance("SHA-256")
        val clientJson = ClientJson(
            type = type,
            challenge = challenge,
            origin = appInfoToOrigin(callingAppInfo),
            androidPackageName = callingAppInfo.packageName,
        )
        md.digest(Json.encodeToString(clientJson).toByteArray())
    }
    return b64Encode(dataHash)
}