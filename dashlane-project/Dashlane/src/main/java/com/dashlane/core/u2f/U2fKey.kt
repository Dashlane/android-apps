package com.dashlane.core.u2f

import android.util.Base64
import com.dashlane.core.u2f.transport.NfcTransport
import com.dashlane.core.u2f.transport.Transport
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.nio.charset.Charset

class U2fKey(val transport: Transport) {

    val requireUserAction = transport !is NfcTransport

    suspend fun signChallenges(challenges: List<U2fChallenge>) = withContext(Dispatchers.IO) {
        transport.use {
            if (!it.init()) return@use null
            for (challenge in challenges) {
                if (challenge.version != U2fChallenge.U2F_VERSION_U2FV2) continue
                val resp = it.sign(challenge) ?: continue

                return@use SignedChallenge(
                    challenge = challenge.challenge,
                    signatureData = Base64.encodeToString(resp, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING),
                    clientData = Base64.encodeToString(
                        challenge.clientDataString?.toByteArray(Charset.forName("ASCII")),
                        Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING
                    ),
                    keyHandle = challenge.keyHandle
                )
            }
            null
        }
    }

    data class SignedChallenge(
        val challenge: String,
        val signatureData: String,
        val clientData: String,
        val keyHandle: String
    ) {
        fun toJson() =
            JSONObject().apply {
                put("signatureData", signatureData)
                put("clientData", clientData)
                put("keyHandle", keyHandle)
            }.toString()
    }
}