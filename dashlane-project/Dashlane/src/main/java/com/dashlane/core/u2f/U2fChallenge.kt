package com.dashlane.core.u2f

import com.dashlane.cryptography.decodeBase64ToByteArray
import com.dashlane.util.tryOrNull
import org.json.JSONObject



data class U2fChallenge(
    val version: String,
    val origin: String,
    val keyHandle: String,
    val challenge: String
) {
    val keyHandleBytes: ByteArray
        get() = keyHandle.decodeBase64ToByteArray()

    val clientDataString: String?
        get() {
            return tryOrNull {
                JSONObject().apply {
                    put(U2F_AUTHENTICATION_TYP_KEY, U2F_AUTHENTICATION_TYP)
                    put(U2F_AUTHENTICATION_CHALLENGE_KEY, challenge)
                        .put(U2F_AUTHENTICATION_ORIGIN_KEY, origin)
                }.toString()
            }
        }

    companion object {
        const val U2F_VERSION_U2FV2 = "U2F_V2"
        const val U2F_AUTHENTICATION_TYP = "navigator.id.getAssertion"
        const val U2F_AUTHENTICATION_TYP_KEY = "typ"
        const val U2F_AUTHENTICATION_CHALLENGE_KEY = "challenge"
        const val U2F_AUTHENTICATION_ORIGIN_KEY = "origin"
    }
}
