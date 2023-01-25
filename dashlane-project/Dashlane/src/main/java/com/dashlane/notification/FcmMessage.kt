package com.dashlane.notification

import com.dashlane.util.tryOrNull
import org.json.JSONObject



data class FcmMessage(
    val data: String? = null,
    val login: String? = null,
    val from: String? = null,
    val code: FcmCode,
    val message: String? = null
)

fun FcmMessage.getJsonData(key: String): Any? = tryOrNull {
    JSONObject(data!!).opt(key)
}