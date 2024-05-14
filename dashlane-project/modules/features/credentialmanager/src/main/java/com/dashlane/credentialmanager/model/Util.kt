package com.dashlane.credentialmanager.model

import androidx.annotation.RequiresApi
import androidx.credentials.provider.CallingAppInfo
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