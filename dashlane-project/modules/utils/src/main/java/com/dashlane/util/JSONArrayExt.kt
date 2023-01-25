package com.dashlane.util

import org.json.JSONArray

fun JSONArray.asOptStringSequence() =
    asSequence(JSONArray::optString)

fun JSONArray.asOptJSONObjectSequence() =
    asSequence(JSONArray::optJSONObject)

inline fun <T> JSONArray.asSequence(crossinline transform: JSONArray.(Int) -> T) =
    (0 until length()).asSequence().map { index -> transform(index) }
